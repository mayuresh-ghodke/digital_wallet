# Digital Wallet System — Idempotency & Concurrency

A secure digital wallet backend (Paytm/PhonePe-style) built with **Spring Boot 3.5.5** and **Java 17**, featuring JWT-based authentication, optimistic-locking concurrency control, and idempotent deposit/transfer APIs.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 3.5.5 (Web, Data JPA, Security, Validation) |
| Database | H2 (in-memory) |
| Auth | JWT (`jjwt` 0.12.5) |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, Spring Security Test |
| Code Coverage | JaCoCo |
| Code Quality | SonarQube (`sonar-maven-plugin`) |

---

## 1. Security Implementation

- **Authentication** is stateless and token-based. `AuthController` exposes `POST /auth/register` and `POST /auth/login`; on successful login `JwtUtils` issues a signed JWT containing the user's `userId`, subject (email), issued-at/expiry, and `role` claim.
- **Password storage**: passwords are hashed with `BCryptPasswordEncoder` (`SecurityConfig`) — plaintext passwords are never persisted or logged.
- **Token verification**: `JwtAuthenticationFilter` (a `OncePerRequestFilter`) runs on every request ahead of Spring Security's own auth filter. It reads the `Authorization: Bearer <token>` header, extracts the username from the JWT, loads the user via `CustomUserDetailsService`, validates the token's signature/expiry against that user, and populates the `SecurityContextHolder` if valid.
- **Signing**: tokens are signed with an HMAC-SHA key derived from `jwt.secret`, which is injected from the `JWT_SECRET` environment variable (with a local-dev fallback in `application.yaml`) — the secret is never hard-coded in source.
- **Stateless sessions**: `SessionCreationPolicy.STATELESS` — no server-side session state, so the app scales horizontally without sticky sessions.
- **Authorization (RBAC)**: enforced at the URL level in `SecurityConfig`:
  - `/auth/**`, `/h2-console/**` → public
  - `/wallet/**` → requires `ROLE_USER`
  - `/admin/**` → requires `ROLE_ADMIN`
  - everything else → requires authentication

  `@EnableMethodSecurity` is also enabled for method-level checks where needed.
- **CSRF** is disabled deliberately — this is a stateless, token-authenticated REST API (no browser cookie/session auth), which is the standard posture for JWT APIs.
- **Input validation**: all request DTOs use Bean Validation (`@NotNull`, `@NotBlank`, `@Email`) and controllers apply `@Valid`; validation failures are caught centrally (see below) instead of leaking stack traces.
- **Least-privilege object exposure**: controllers work off `CustomUserDetails`/`@AuthenticationPrincipal` to derive the acting `userId` from the token — a caller can only ever act on **their own** wallet, never an arbitrary `userId` passed in the request body.

---

## 2. Idempotency Handling

Every state-changing wallet operation (`POST /wallet/add`, `POST /wallet/transfer`) **requires** an `Idempotency-Key` request header supplied by the client.

**Flow (`WalletServiceImpl`):**

1. On each request, the service first looks up `Transaction` by `idempotencyKey` (`transactionService.findByIdempotencyKey`).
2. **If a transaction already exists for that key**, the previously-persisted result is returned immediately — the operation is *not* re-executed, so retried/duplicate network calls (client timeouts, double-clicks, load-balancer retries) can never double-charge or double-credit a wallet.
3. **If no transaction exists**, the actual deposit/transfer logic runs inside `WalletTransactionExecutor`, and the resulting `Transaction` row is persisted with that idempotency key.

**Database-level guarantee:** the `idempotency_key` column on `Transaction` has a `unique` constraint (`@Column(unique = true)`). This is the real source of truth for idempotency — even if two concurrent requests with the *same key* both pass the initial "not found" check (a race between step 1 and step 3), only one `INSERT` can succeed. The loser fails with a `DataIntegrityViolationException`, which is caught and turned into a re-fetch of the now-existing transaction by key, so the client still gets a single consistent result instead of an error or a duplicate transaction.

---

## 3. Concurrency Strategy

Wallet balance updates are protected with **optimistic locking**, not in-memory locks (`synchronized`)/`ReentrantLock`, so correctness holds even across multiple app instances behind a load balancer.

- `Wallet` carries a `@Version` (`Long version`) field. Every `UPDATE` to a wallet row includes `WHERE version = ?`; Hibernate increments the version on write. If two transactions read the same wallet and try to update it concurrently, the second writer's `UPDATE` matches zero rows and Hibernate throws `ObjectOptimisticLockingFailureException`.
- **Retry loop**: `WalletServiceImpl` wraps each deposit/transfer in a bounded retry loop (`MAX_RETRIES_FOR_TRANSACTION = 3`). On `ObjectOptimisticLockingFailureException` it retries the whole executor call (fresh read → recompute → write) up to 3 times before failing the request with a clear `BadRequestException`, rather than retrying forever or silently corrupting balances.
- **Transfers update two wallets** (sender + receiver) in the same executor call, and both writes are flushed (`saveAndFlush`) inside the same transaction — either both balance changes and the transaction record commit together, or the whole attempt is retried/rolled back. There is no window where only one side of a transfer is applied.
- **Self-transfer guard**: a transfer where `senderUserId == toUserId` is rejected before any balance mutation.
- **Balance safety**: null balances/amounts are defensively treated as `BigDecimal.ZERO`, and insufficient-balance transfers are rejected (`InsufficientBalanceException`) before any write occurs.

Optimistic locking was chosen over pessimistic (`SELECT ... FOR UPDATE`) because wallet contention per user is expected to be low — it avoids holding DB row locks across the request and scales better under normal load, while still being 100% correct under concurrent access thanks to the retry loop.

---

## 4. Transaction Boundaries

- **`WalletTransactionExecutorImpl.executeDeposit` / `executeTransfer`** are annotated `@Transactional(propagation = Propagation.REQUIRES_NEW)`. Each retry attempt in `WalletServiceImpl`'s loop therefore runs in a **brand-new, independent database transaction** — a failed/rolled-back attempt (e.g. an optimistic-lock conflict) cannot leave a half-committed transaction hanging around or interfere with the next retry attempt.
- Within one executor call, wallet balance update(s) and the resulting `Transaction` audit row are written **in the same transaction**: `walletRepository.saveAndFlush(...)` followed by `transactionService.saveTransaction(...)`. If persisting the transaction record fails, the balance change in that same attempt is rolled back too — balance and ledger can never drift apart.
- **`TransactionServiceImpl.saveTransaction`** is `@Transactional` (participates in the caller's transaction rather than starting a new one), since it's always invoked from inside the executor's `REQUIRES_NEW` transaction.
- **Read paths** (`getAllTransactions*`, wallet lookups) are marked `@Transactional(readOnly = true)` where applicable, so the persistence layer can apply read-only optimizations and these calls never accidentally hold a write lock.
- **Idempotency lookups** (`findByIdempotencyKey`) happen *outside* the `REQUIRES_NEW` executor transaction, in the service layer, so a duplicate-key short-circuit never opens a DB transaction at all.

---

## 5. Running the Application Locally

### Prerequisites
- JDK 17+
- Maven 3.9+ (or use the bundled wrapper `./mvnw`)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/mayuresh-ghodke/digital_wallet.git
cd digital_wallet

# 2. (Optional) set a custom JWT secret and expiry — otherwise safe local defaults are used
export JWT_SECRET=your-256-bit-secret-key-goes-here
export JWT_EXPIRATION=3600000   # milliseconds

# 3. Build and run
./mvnw spring-boot:run
# or
mvn clean spring-boot:run
```

The app starts on **`http://localhost:8020`**.

- **H2 in-memory database** — no external DB setup needed. Data resets on every restart.
- **H2 console**: `http://localhost:8020/h2-console`
  - JDBC URL: `jdbc:h2:mem:digital_wallet_db`
  - Username: `sa` / Password: *(blank)*

### Quick API walkthrough

```bash
# Register
curl -X POST http://localhost:8020/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@example.com","password":"Passw0rd!"}'

# Login -> returns JWT
curl -X POST http://localhost:8020/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@example.com","password":"Passw0rd!"}'

# Deposit (Idempotency-Key required)
curl -X POST http://localhost:8020/wallet/add \
  -H "Authorization: Bearer <JWT>" \
  -H "Idempotency-Key: <any-unique-uuid>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500}'

# Transfer (Idempotency-Key required)
curl -X POST http://localhost:8020/wallet/transfer \
  -H "Authorization: Bearer <JWT>" \
  -H "Idempotency-Key: <any-unique-uuid>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100, "toUserId": 2}'

# Transaction history
curl http://localhost:8020/wallet/transactions -H "Authorization: Bearer <JWT>"

# Admin (requires an ADMIN-role user)
curl http://localhost:8020/admin/wallets -H "Authorization: Bearer <ADMIN_JWT>"
```

---

## 6. Unit Tests & Coverage

The test suite covers controllers, services, security, and exception handling:

- `AuthControllerTest`, `WalletControllerTest`, `AdminControllerTest`
- `AuthServiceImplTest`, `WalletServiceImplTest`, `TransactionServiceImplTest`, `AdminServiceImplTest`, `WalletTransactionExecutorImplTest`
- `JwtUtilsTest`, `JwtAuthenticationFilterTest`, `CustomUserDetailsServiceTest`
- `GlobalExceptionHandlerTest`

Run the tests and generate a coverage report with JaCoCo:

```bash
mvn clean test
```

This produces a coverage report at:

```
target/site/jacoco/index.html
```

Open that file in a browser to see line/branch coverage per class (target: **>= 80%**).

### Static analysis (SonarQube)

If you have a local SonarQube server running (default `http://localhost:9000`):

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=digital-wallet \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-sonar-token>
```

---

## 7. API Summary

| Method | Endpoint | Role | Idempotency-Key |
|---|---|---|---|
| POST | `/auth/register` | Public | — |
| POST | `/auth/login` | Public | — |
| GET | `/wallet` | USER | — |
| POST | `/wallet/add` | USER | Required |
| POST | `/wallet/transfer` | USER | Required |
| GET | `/wallet/transactions` | USER | — |
| GET | `/admin/wallets` | ADMIN | — |
| GET | `/admin/transactions` | ADMIN | — |

---

## 8. Design Assumptions

- One wallet per user, created implicitly at registration (1:1 `User` <-> `Wallet`).
- All monetary amounts use `BigDecimal(precision=19, scale=4)` to avoid floating-point rounding errors.
- Idempotency keys are supplied by the **client** (typically a UUID generated per logical operation) and are unique per transaction attempt, not per user.
- `ddl-auto: update` and H2 are used.
- ## 8. Design Assumptions
 
- One wallet per user, created implicitly at registration (1:1 `User` <-> `Wallet`).
- All monetary amounts use `BigDecimal(precision=19, scale=4)` to avoid floating-point rounding errors.
- Idempotency keys are supplied by the **client** (typically a UUID generated per logical operation) and are unique per transaction attempt, not per user.
- `ddl-auto: update` and H2 are used.
- There is no public API endpoint to create an `ADMIN` user — `/auth/register` only ever creates `USER` role accounts, by design, to avoid exposing privilege escalation via the API. For local testing/demo purposes, an `ADMIN` user is seeded manually via a direct SQL `INSERT` (through the H2 console) rather than through the registration API. In a production system this would instead be handled through a separate, tightly-controlled admin-provisioning process.
** SQL Insert Query: 
INSERT INTO users (email, password, role) VALUES ('admin@example.com', '$2b$10$OZvhY8sBVRkfMk./jAy7Tu.bt9ht7Zoz5xepzJVhwIcvH5oE65Rw6', 'ADMIN');
