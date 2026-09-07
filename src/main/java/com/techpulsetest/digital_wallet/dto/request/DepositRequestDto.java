package com.techpulsetest.digital_wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequestDto {

     @NotNull(message = "Amount is required to deposit")
     private BigDecimal amount;
}
