package com.techpulsetest.digital_wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {
    private Integer transactionId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String transactionType;
    private String transactionStatus;
    private String failureReason;
    private LocalDateTime createdAt;
}
