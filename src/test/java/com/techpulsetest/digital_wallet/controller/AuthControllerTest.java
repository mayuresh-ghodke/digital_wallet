package com.techpulsetest.digital_wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulsetest.digital_wallet.dto.request.LoginRequestDto;
import com.techpulsetest.digital_wallet.dto.request.RegisterRequestDto;
import com.techpulsetest.digital_wallet.dto.response.AuthResponseDto;
import com.techpulsetest.digital_wallet.security.CustomUserDetailsService;
import com.techpulsetest.digital_wallet.security.JwtUtils;
import com.techpulsetest.digital_wallet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void registerUser_ReturnsCreated() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("pass1234");

        AuthResponseDto response = AuthResponseDto.builder().email("user@example.com").token("jwt-token").build();

        when(authService.registerUser(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_ReturnsOk() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("pass1234");

        AuthResponseDto response = AuthResponseDto.builder().email("user@example.com").token("jwt-token").build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}