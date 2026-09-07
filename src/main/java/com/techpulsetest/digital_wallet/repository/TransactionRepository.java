package com.techpulsetest.digital_wallet.repository;

import com.techpulsetest.digital_wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findByFromWallet_User_IdOrToWallet_User_Id(
            Integer fromUserId,
            Integer toUserId,
            Pageable pageable
    );

    Page<Transaction> findByFromWallet_WalletIdOrToWallet_WalletId(
            Integer fromWalletId,
            Integer toWalletId,
            Pageable pageable
    );
}
