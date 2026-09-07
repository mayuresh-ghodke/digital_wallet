package com.techpulsetest.digital_wallet.service.impl;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.mapper.TransactionMapper;
import com.techpulsetest.digital_wallet.mapper.WalletMapper;
import com.techpulsetest.digital_wallet.repository.TransactionRepository;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponseDto> getAllWallets() {
        return walletRepository.findAll()
                .stream()
                .map(walletMapper::mapWalletToWalletResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::mapTransactionToResponseDto)
                .toList();
    }
}
