package com.techpulsetest.digital_wallet.service.impl;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.exceptions.BadRequestException;
import com.techpulsetest.digital_wallet.exceptions.ResourceNotFoundException;
import com.techpulsetest.digital_wallet.mapper.WalletMapper;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.TransactionService;
import com.techpulsetest.digital_wallet.service.WalletService;
import com.techpulsetest.digital_wallet.service.WalletTransactionExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final WalletMapper walletMapper;
    private final WalletTransactionExecutor walletTransactionExecutor;

    private static final int MAX_RETRIES_FOR_TRANSACTION = 3;

    @Override
    public WalletResponseDto getWalletByUserId(Integer userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for User ID: " + userId));
        return walletMapper.mapWalletToWalletResponse(wallet);
    }

    @Override
    public TransactionResponseDto depositAmount(Integer userId, String idempotencyKey, DepositRequestDto depositRequestDto) {

        Optional<TransactionResponseDto> existingTransactionResponse = transactionService.findByIdempotencyKey(idempotencyKey);
        if (existingTransactionResponse.isPresent()) {
            return existingTransactionResponse.get();
        }

        int attempts = 0;
        while (attempts < MAX_RETRIES_FOR_TRANSACTION) {
            try {
                return walletTransactionExecutor.executeDeposit(userId, idempotencyKey, depositRequestDto);
            }
            catch (ObjectOptimisticLockingFailureException exception) {
                attempts++;
            }
            catch (DataIntegrityViolationException exception) {
                return transactionService.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow(() -> exception);
            }
        }
        throw new BadRequestException("Deposit failed after maximum retries. Please try again.");
    }

    @Override
    public TransactionResponseDto transferAmount(Integer senderUserId, String idempotencyKey, TransferRequestDto transferRequestDto) {

        Optional<TransactionResponseDto> existingTransactionResponse = transactionService.findByIdempotencyKey(idempotencyKey);
        if (existingTransactionResponse.isPresent()) {
            return existingTransactionResponse.get();
        }

        int attempts = 0;
        while (attempts < MAX_RETRIES_FOR_TRANSACTION) {
            try {
                return walletTransactionExecutor.executeTransfer(senderUserId, idempotencyKey, transferRequestDto);
            }
            catch (ObjectOptimisticLockingFailureException exception) {
                attempts++;
            }
            catch (DataIntegrityViolationException exception) {
                return transactionService.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow(() -> exception);
            }
        }
        throw new BadRequestException("Transfer failed after maximum retries. Please try again.");
    }
}
