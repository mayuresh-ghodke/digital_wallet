package com.techpulsetest.digital_wallet.service.impl;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.enums.TransactionStatus;
import com.techpulsetest.digital_wallet.enums.TransactionType;
import com.techpulsetest.digital_wallet.exceptions.BadRequestException;
import com.techpulsetest.digital_wallet.exceptions.InsufficientBalanceException;
import com.techpulsetest.digital_wallet.exceptions.ResourceNotFoundException;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.TransactionService;
import com.techpulsetest.digital_wallet.service.WalletTransactionExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletTransactionExecutorImpl implements WalletTransactionExecutor {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionResponseDto executeDeposit(Integer userId, String idempotencyKey, DepositRequestDto depositRequest) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with ID-" + userId));

        // Safeguard against null balances or null deposit amounts causing NullPointerException
        BigDecimal currentBalance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        BigDecimal depositAmount = depositRequest.getAmount() != null ? depositRequest.getAmount() : BigDecimal.ZERO;

        wallet.setBalance(currentBalance.add(depositAmount));
        walletRepository.saveAndFlush(wallet);

        return transactionService.saveTransaction(
                idempotencyKey, null, wallet, depositAmount,
                TransactionType.ADD, TransactionStatus.SUCCESS, null
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionResponseDto executeTransfer(Integer senderUserId, String idempotencyKey, TransferRequestDto transferRequest) {
        if (senderUserId.equals(transferRequest.getToUserId())) {
            throw new BadRequestException("Sender and receiver must be different.");
        }

        Wallet senderWallet = walletRepository.findByUser_Id(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet for sender not found with ID-" + senderUserId));

        Wallet receiverWallet = walletRepository.findByUser_Id(transferRequest.getToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet for receiver not found with ID-" + transferRequest.getToUserId()));

        // Safeguard against null balances or null transfer amounts
        BigDecimal senderBalance = senderWallet.getBalance() != null ? senderWallet.getBalance() : BigDecimal.ZERO;
        BigDecimal receiverBalance = receiverWallet.getBalance() != null ? receiverWallet.getBalance() : BigDecimal.ZERO;
        BigDecimal transferAmount = transferRequest.getAmount() != null ? transferRequest.getAmount() : BigDecimal.ZERO;

        if (senderBalance.compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance.");
        }

        senderWallet.setBalance(senderBalance.subtract(transferAmount));
        receiverWallet.setBalance(receiverBalance.add(transferAmount));

        walletRepository.saveAndFlush(senderWallet);
        walletRepository.saveAndFlush(receiverWallet);

        return transactionService.saveTransaction(
                idempotencyKey, senderWallet, receiverWallet, transferAmount,
                TransactionType.TRANSFER, TransactionStatus.SUCCESS, null
        );
    }
}