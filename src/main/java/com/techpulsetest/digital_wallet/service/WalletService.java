package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;

public interface WalletService {
    WalletResponseDto getWalletByUserId(Integer userId);
    TransactionResponseDto depositAmount(Integer userId, String idempotencyKey, DepositRequestDto depositRequestDto);
    TransactionResponseDto transferAmount(Integer sendUserId, String idempotencyKey, TransferRequestDto transferRequestDto);
}