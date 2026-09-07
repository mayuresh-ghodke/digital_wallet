package com.techpulsetest.digital_wallet.mapper;

import com.techpulsetest.digital_wallet.dto.request.RegisterRequestDto;
import com.techpulsetest.digital_wallet.dto.response.AuthResponseDto;
import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User mapRegisterRequestToUser(RegisterRequestDto requestDto, String encodedPassword) {
        return User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public AuthResponseDto mapUserToAuthResponse(User user, String token) {
        return new AuthResponseDto(token, user.getEmail());
    }
}
