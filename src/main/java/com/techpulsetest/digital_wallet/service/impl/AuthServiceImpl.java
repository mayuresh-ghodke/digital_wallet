package com.techpulsetest.digital_wallet.service.impl;

import com.techpulsetest.digital_wallet.dto.request.LoginRequestDto;
import com.techpulsetest.digital_wallet.dto.request.RegisterRequestDto;
import com.techpulsetest.digital_wallet.dto.response.AuthResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.entity.Wallet;
import com.techpulsetest.digital_wallet.exceptions.DuplicateResourceException;
import com.techpulsetest.digital_wallet.mapper.UserMapper;
import com.techpulsetest.digital_wallet.repository.UserRepository;
import com.techpulsetest.digital_wallet.repository.WalletRepository;
import com.techpulsetest.digital_wallet.security.CustomUserDetails;
import com.techpulsetest.digital_wallet.security.JwtUtils;
import com.techpulsetest.digital_wallet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponseDto registerUser(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(registerRequestDto.getPassword());
        User user = userMapper.mapRegisterRequestToUser(registerRequestDto, encodedPassword);
        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        String token = jwtUtils.generateToken(new CustomUserDetails(savedUser));
        return userMapper.mapUserToAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
            );
        }
        catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        String token = jwtUtils.generateToken(new CustomUserDetails(user));
        return userMapper.mapUserToAuthResponse(user, token);
    }
}
