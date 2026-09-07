package com.techpulsetest.digital_wallet.mapper;

import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponseDto mapWalletToWalletResponse(Wallet wallet) {
        if(wallet == null)
            return null;
        return WalletResponseDto.builder()
                .walletId(wallet.getWalletId())
                .balance(wallet.getBalance())
                .build();
    }
}
