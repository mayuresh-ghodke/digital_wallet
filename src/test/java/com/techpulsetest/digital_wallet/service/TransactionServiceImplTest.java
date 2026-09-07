package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.Transaction;
import com.techpulsetest.digital_wallet.enums.TransactionStatus;
import com.techpulsetest.digital_wallet.enums.TransactionType;
import com.techpulsetest.digital_wallet.mapper.TransactionMapper;
import com.techpulsetest.digital_wallet.repository.TransactionRepository;
import com.techpulsetest.digital_wallet.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(1)
                .idempotencyKey("idem-123")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.ADD)
                .status(TransactionStatus.SUCCESS)
                .build();
    }

    @Test
    void findByIdempotencyKey_Found() {
        when(transactionRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.of(transaction));

        Optional<TransactionResponseDto> result = transactionService.findByIdempotencyKey("idem-123");

        assertTrue(result.isPresent());
        assertEquals("idem-123", result.get().getIdempotencyKey());
    }

    @Test
    void saveTransaction_Success() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDto result = transactionService.saveTransaction(
                "idem-123", null, null, BigDecimal.valueOf(100.00),
                TransactionType.ADD, TransactionStatus.SUCCESS, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTransactionId());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getAllTransactionsByUserId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(transaction));

        when(transactionRepository.findByFromWallet_User_IdOrToWallet_User_Id(1, 1, pageable)).thenReturn(page);

        Page<TransactionResponseDto> result = transactionService.getAllTransactionsByUserId(1, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllTransactionsByWalletId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(transaction));

        when(transactionRepository.findByFromWallet_WalletIdOrToWallet_WalletId(10, 10, pageable)).thenReturn(page);

        Page<TransactionResponseDto> result = transactionService.getAllTransactionsByWalletId(10, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllTransactions_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(transaction));

        when(transactionRepository.findAll(pageable)).thenReturn(page);

        Page<TransactionResponseDto> result = transactionService.getAllTransactions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}