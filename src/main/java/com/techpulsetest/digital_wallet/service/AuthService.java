package com.techpulsetest.digital_wallet.service;

import com.techpulsetest.digital_wallet.dto.request.LoginRequestDto;
import com.techpulsetest.digital_wallet.dto.request.RegisterRequestDto;
import com.techpulsetest.digital_wallet.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto registerUser(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
}
