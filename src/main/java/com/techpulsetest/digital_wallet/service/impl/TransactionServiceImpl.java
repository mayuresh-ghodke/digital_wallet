package com.techpulsetest.digital_wallet.service.impl;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.Transaction;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.enums.TransactionStatus;
import com.techpulsetest.digital_wallet.enums.TransactionType;
import com.techpulsetest.digital_wallet.mapper.TransactionMapper;
import com.techpulsetest.digital_wallet.repository.TransactionRepository;
import com.techpulsetest.digital_wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Optional<TransactionResponseDto> findByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .map(transactionMapper::mapTransactionToResponseDto);
    }

    @Override
    @Transactional
    public TransactionResponseDto saveTransaction(String idempotencyKey, Wallet fromWallet, Wallet toWallet, BigDecimal amount, TransactionType transactionType, TransactionStatus transactionStatus, String failureReason) {
        Transaction transaction = Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(amount)
                .type(transactionType)
                .status(transactionStatus)
                .failureReason(failureReason)
                .build();

        return transactionMapper.mapTransactionToResponseDto(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAllTransactionsByUserId(Integer userId, Pageable pageable) {
        return transactionRepository
                .findByFromWallet_User_IdOrToWallet_User_Id(userId, userId, pageable)
                .map(transactionMapper::mapTransactionToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAllTransactionsByWalletId(Integer walletId, Pageable pageable) {
        return transactionRepository
                .findByFromWallet_WalletIdOrToWallet_WalletId(walletId, walletId, pageable)
                .map(transactionMapper::mapTransactionToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::mapTransactionToResponseDto);
    }
}
