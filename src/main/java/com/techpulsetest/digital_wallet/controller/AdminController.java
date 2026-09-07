package com.techpulsetest.digital_wallet.controller;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/wallets")
    public ResponseEntity<List<WalletResponseDto>> getAllWallets() {
        return ResponseEntity.ok(adminService.getAllWallets());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllTransactions(pageable));
    }
}
