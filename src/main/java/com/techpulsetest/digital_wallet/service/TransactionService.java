package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.enums.TransactionStatus;
import com.techpulsetest.digital_wallet.enums.TransactionType;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionService {

    Optional<TransactionResponseDto> findByIdempotencyKey(String idempotencyKey);

    TransactionResponseDto saveTransaction(String idempotencyKey, Wallet fromWallet, Wallet toWallet,
                                           BigDecimal amount, TransactionType transactionType, TransactionStatus transactionStatus,
                                           String failureReason);

    Page<TransactionResponseDto> getAllTransactionsByUserId(Integer userId, Pageable pageable);

    Page<TransactionResponseDto> getAllTransactionsByWalletId(Integer walletId, Pageable pageable);

    Page<TransactionResponseDto> getAllTransactions(Pageable pageable);
}
