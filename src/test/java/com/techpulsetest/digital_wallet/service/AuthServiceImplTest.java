package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.LoginRequestDto;
import com.techpulsetest.digital_wallet.dto.request.RegisterRequestDto;
import com.techpulsetest.digital_wallet.dto.response.AuthResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.enums.Role;
import com.techpulsetest.digital_wallet.exceptions.DuplicateResourceException;
import com.techpulsetest.digital_wallet.mapper.UserMapper;
import com.techpulsetest.digital_wallet.repository.UserRepository;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.security.CustomUserDetails;
import com.techpulsetest.digital_wallet.security.JwtUtils;
import com.techpulsetest.digital_wallet.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto registerDto;
    private LoginRequestDto loginDto;
    private User user;

    @BeforeEach
    void setUp() {
        registerDto = new RegisterRequestDto();
        registerDto.setEmail("user@example.com");
        registerDto.setPassword("pass123");

        loginDto = new LoginRequestDto();
        loginDto.setEmail("user@example.com");
        loginDto.setPassword("pass123");

        user = User.builder()
                .id(1)
                .email("user@example.com")
                .password("encodedPass")
                .role(Role.USER)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token");

        AuthResponseDto result = authService.registerUser(registerDto);

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        assertEquals("jwt-token", result.getToken());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.registerUser(registerDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any())).thenReturn("mock-jwt-token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
    }
}