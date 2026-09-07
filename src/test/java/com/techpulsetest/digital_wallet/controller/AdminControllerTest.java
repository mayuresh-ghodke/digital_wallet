package com.techpulsetest.digital_wallet.controller;

import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.security.CustomUserDetailsService;
import com.techpulsetest.digital_wallet.security.JwtUtils;
import com.techpulsetest.digital_wallet.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getAllWallets_ReturnsOk() throws Exception {
        WalletResponseDto wallet = WalletResponseDto.builder().walletId(1).balance(BigDecimal.valueOf(100.00)).build();
        when(adminService.getAllWallets()).thenReturn(Collections.singletonList(wallet));

        mockMvc.perform(get("/admin/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].walletId").value(1));
    }

    @Test
    void getAllTransactions_ReturnsOk() throws Exception {
        TransactionResponseDto tx = TransactionResponseDto.builder().transactionId(10).build();
        when(adminService.getAllTransactions(any(Pageable.class))).thenReturn(Collections.singletonList(tx));

        mockMvc.perform(get("/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(10));
    }
}