package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.exceptions.ResourceNotFoundException;
import com.techpulsetest.digital_wallet.mapper.WalletMapper;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionExecutor walletTransactionExecutor;

    @Mock
    private TransactionService transactionService;

    @Spy
    private WalletMapper walletMapper = new WalletMapper();

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1).email("test@example.com").build();
        wallet = Wallet.builder().walletId(10).user(user).balance(BigDecimal.valueOf(100.00)).build();
    }

    @Test
    void getWalletByUserId_Success() {
        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.of(wallet));

        WalletResponseDto response = walletService.getWalletByUserId(1);

        assertNotNull(response);
        assertEquals(10, response.getWalletId());
        assertEquals(BigDecimal.valueOf(100.00), response.getBalance());
    }

    @Test
    void getWalletByUserId_NotFound_ThrowsException() {
        when(walletRepository.findByUser_Id(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> walletService.getWalletByUserId(1));
    }

    @Test
    void depositAmount_DelegatesToExecutor() {
        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(BigDecimal.valueOf(50.00));
        TransactionResponseDto expected = TransactionResponseDto.builder().transactionId(100).build();

        when(walletTransactionExecutor.executeDeposit(1, "key-1", request)).thenReturn(expected);

        TransactionResponseDto response = walletService.depositAmount(1, "key-1", request);

        assertNotNull(response);
        assertEquals(100, response.getTransactionId());
        verify(walletTransactionExecutor, times(1)).executeDeposit(1, "key-1", request);
    }

    @Test
    void transferAmount_DelegatesToExecutor() {
        TransferRequestDto request = new TransferRequestDto();
        request.setAmount(BigDecimal.valueOf(20.00));
        request.setToUserId(2);

        TransactionResponseDto expected = TransactionResponseDto.builder().transactionId(101).build();

        when(walletTransactionExecutor.executeTransfer(1, "key-2", request)).thenReturn(expected);

        TransactionResponseDto response = walletService.transferAmount(1, "key-2", request);

        assertNotNull(response);
        assertEquals(101, response.getTransactionId());
        verify(walletTransactionExecutor, times(1)).executeTransfer(1, "key-2", request);
    }
}