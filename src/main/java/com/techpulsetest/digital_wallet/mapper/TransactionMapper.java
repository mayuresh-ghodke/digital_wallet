package com.techpulsetest.digital_wallet.mapper;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDto mapTransactionToResponseDto(Transaction transaction) {
        if(transaction == null)
            return null;

        return TransactionResponseDto.builder()
                .transactionId(transaction.getId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .amount(transaction.getAmount())
                .transactionType(transaction.getType().toString())
                .transactionStatus(transaction.getStatus().toString())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
