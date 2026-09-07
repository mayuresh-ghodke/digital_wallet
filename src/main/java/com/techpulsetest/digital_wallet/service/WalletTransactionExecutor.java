package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;

public interface WalletTransactionExecutor {
    TransactionResponseDto executeDeposit(Integer userId, String idempotencyKey, DepositRequestDto depositRequest);
    TransactionResponseDto executeTransfer(Integer senderUserId, String idempotencyKey, TransferRequestDto transferRequest);
}
