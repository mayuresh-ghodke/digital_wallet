package com.techpulsetest.digital_wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulsetest.digital_wallet.dto.request.DepositRequestDto;
import com.techpulsetest.digital_wallet.dto.request.TransferRequestDto;
import com.techpulsetest.digital_wallet.dto.response.TransactionResponseDto;
import com.techpulsetest.digital_wallet.dto.response.WalletResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.enums.Role;
import com.techpulsetest.digital_wallet.security.CustomUserDetails;
import com.techpulsetest.digital_wallet.security.CustomUserDetailsService;
import com.techpulsetest.digital_wallet.security.JwtUtils;
import com.techpulsetest.digital_wallet.service.TransactionService;
import com.techpulsetest.digital_wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private void setupSecurityContext(Integer userId) {
        User user = User.builder().id(userId).email("user@example.com").role(Role.USER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getWalletByUserId_ReturnsOk() throws Exception {
        setupSecurityContext(1);

        WalletResponseDto response = WalletResponseDto.builder().walletId(10).balance(BigDecimal.valueOf(100.00)).build();
        when(walletService.getWalletByUserId(1)).thenReturn(response);

        mockMvc.perform(get("/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(10))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void addMoney_ReturnsOk() throws Exception {
        setupSecurityContext(1);

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(BigDecimal.valueOf(50.00));

        TransactionResponseDto response = TransactionResponseDto.builder().transactionId(1).amount(BigDecimal.valueOf(50.00)).build();

        when(walletService.depositAmount(eq(1), eq("key-123"), any(DepositRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/wallet/add")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));
    }

    @Test
    void transferMoney_ReturnsOk() throws Exception {
        setupSecurityContext(1);

        TransferRequestDto request = new TransferRequestDto();
        request.setAmount(BigDecimal.valueOf(20.00));
        request.setToUserId(2);

        TransactionResponseDto response = TransactionResponseDto.builder().transactionId(2).amount(BigDecimal.valueOf(20.00)).build();

        when(walletService.transferAmount(eq(1), eq("key-456"), any(TransferRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/wallet/transfer")
                        .header("Idempotency-Key", "key-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(2));
    }

    @Test
    void getTransactionHistory_ReturnsPagedResponse() throws Exception {
        setupSecurityContext(1);

        TransactionResponseDto dto = TransactionResponseDto.builder().transactionId(1).build();
        when(transactionService.getAllTransactionsByUserId(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(dto)));

        mockMvc.perform(get("/wallet/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value(1));
    }
}