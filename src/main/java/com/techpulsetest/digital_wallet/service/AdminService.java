package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    List<WalletResponseDto> getAllWallets();
    List<TransactionResponseDto> getAllTransactions(Pageable pageable);
}
