package com.techpulsetest.digital_wallet.controller;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.security.CustomUserDetails;
import com.techpulsetest.digital_wallet.service.TransactionService;
import com.techpulsetest.digital_wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<WalletResponseDto> getWalletByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getUserId();
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<TransactionResponseDto> addMoney(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid  @RequestBody DepositRequestDto depositRequest)  {

        Integer userId = userDetails.getUserId();
        return ResponseEntity.ok(walletService.depositAmount(userId, idempotencyKey, depositRequest));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transferMoney(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequestDto transferRequest) {

        Integer userId = customUserDetails.getUserId();
        return ResponseEntity.ok(walletService.transferAmount(userId, idempotencyKey, transferRequest));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable) {
        Integer userId = customUserDetails.getUserId();
        return ResponseEntity.ok(transactionService.getAllTransactionsByUserId(userId, pageable));
    }
}
