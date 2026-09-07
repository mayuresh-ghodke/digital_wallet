package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.exceptions.InsufficientBalanceException;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.impl.WalletTransactionExecutorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletTransactionExecutorImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletTransactionExecutorImpl executor;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        User user1 = User.builder().id(1).build();
        User user2 = User.builder().id(2).build();

        senderWallet = Wallet.builder().walletId(10).user(user1).balance(BigDecimal.valueOf(100.00)).build();
        receiverWallet = Wallet.builder().walletId(20).user(user2).balance(BigDecimal.valueOf(50.00)).build();
    }

    @Test
    void executeDeposit_Success() {
        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(BigDecimal.valueOf(50.00));

        when(transactionService.findByIdempotencyKey("idem-dep-1")).thenReturn(Optional.empty());
        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.of(senderWallet));
        when(transactionService.saveTransaction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TransactionResponseDto.builder().transactionId(1).build());

        TransactionResponseDto response = executor.executeDeposit(1, "idem-dep-1", request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(150.00), senderWallet.getBalance());

        verify(walletRepository, times(1)).saveAndFlush(senderWallet);
    }

    @Test
    void executeDeposit_IdempotentDuplicate_ReturnsExisting() {
        DepositRequestDto request = new DepositRequestDto();
        TransactionResponseDto existingResponse = TransactionResponseDto.builder().transactionId(99).build();

        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.of(senderWallet));
        when(transactionService.findByIdempotencyKey("idem-dep-1")).thenReturn(Optional.of(existingResponse));

        TransactionResponseDto response = executor.executeDeposit(1, "idem-dep-1", request);

        assertEquals(99, response.getTransactionId());
        verify(walletRepository, never()).saveAndFlush(any());
    }

    @Test
    void executeTransfer_Success() {
        TransferRequestDto request = new TransferRequestDto();
        request.setAmount(BigDecimal.valueOf(40.00));
        request.setToUserId(2);

        when(transactionService.findByIdempotencyKey("idem-trf-1")).thenReturn(Optional.empty());
        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser_Id(2)).thenReturn(Optional.of(receiverWallet));
        when(transactionService.saveTransaction(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TransactionResponseDto.builder().transactionId(2).build());

        TransactionResponseDto response = executor.executeTransfer(1, "idem-trf-1", request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(60.00), senderWallet.getBalance());
        assertEquals(BigDecimal.valueOf(90.00), receiverWallet.getBalance());

        verify(walletRepository, times(1)).saveAndFlush(senderWallet);
        verify(walletRepository, times(1)).saveAndFlush(receiverWallet);
    }

    @Test
    void executeTransfer_InsufficientBalance_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setAmount(BigDecimal.valueOf(500.00));
        request.setToUserId(2);

        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser_Id(2)).thenReturn(Optional.of(receiverWallet));

        assertThrows(InsufficientBalanceException.class, () -> executor.executeTransfer(1, "idem-trf-1", request));
    }
}