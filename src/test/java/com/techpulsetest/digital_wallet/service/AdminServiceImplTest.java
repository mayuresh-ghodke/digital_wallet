package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.entity.Transaction;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.enums.TransactionStatus;
import com.techpulsetest.digital_wallet.enums.TransactionType;
import com.techpulsetest.digital_wallet.mapper.TransactionMapper;
import com.techpulsetest.digital_wallet.mapper.WalletMapper;
import com.techpulsetest.digital_wallet.repository.TransactionRepository;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getAllWallets_ReturnsList() {
        Wallet wallet = Wallet.builder().walletId(1).balance(BigDecimal.valueOf(250.00)).build();

        WalletResponseDto expectedDto = WalletResponseDto.builder()
                .walletId(1)
                .balance(BigDecimal.valueOf(250.00))
                .build();

        when(walletRepository.findAll()).thenReturn(Collections.singletonList(wallet));

        when(walletMapper.mapWalletToWalletResponse(wallet)).thenReturn(expectedDto);

        List<WalletResponseDto> result = adminService.getAllWallets();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getWalletId());
    }

    @Test
    void getAllTransactions_ReturnsList() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction tx = Transaction.builder()
                .id(5)
                .amount(BigDecimal.valueOf(50.00))
                .type(TransactionType.ADD)
                .status(TransactionStatus.SUCCESS)
                .build();

        TransactionResponseDto expectedDto = TransactionResponseDto.builder()
                .transactionId(5)
                .amount(BigDecimal.valueOf(50.00))
                .build();

        when(transactionRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.singletonList(tx)));

        when(transactionMapper.mapTransactionToResponseDto(tx)).thenReturn(expectedDto);

        List<TransactionResponseDto> result = adminService.getAllTransactions(pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getTransactionId());
    }
}