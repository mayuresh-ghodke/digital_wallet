package com.techpulsetest.digital_wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDto {
    @NotNull(message = "Amount is required to transfer.")
    private BigDecimal amount;

    @NotNull(message = "User ID is required to transfer amount.")
    private Integer toUserId;
}
