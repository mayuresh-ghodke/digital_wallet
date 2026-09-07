package com.techpulsetest.digital_wallet.security;

import com.techpulsetest.digital_wallet.entity.User;
import com.techpulsetest.digital_wallet.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private CustomUserDetails customUserDetails;

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final Long EXPIRATION = 3600000L;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();

        Field secretField = JwtUtils.class.getDeclaredField("SECRET_KEY");
        secretField.setAccessible(true);
        secretField.set(jwtUtils, SECRET);

        Field expField = JwtUtils.class.getDeclaredField("EXPIRATION");
        expField.setAccessible(true);
        expField.set(jwtUtils, EXPIRATION);

        User user = new User();
        user.setId(1);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);

        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtils.generateToken(customUserDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_Success() {
        String token = jwtUtils.generateToken(customUserDetails);

        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals("user@example.com", extractedUsername);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtils.generateToken(customUserDetails);

        boolean isValid = jwtUtils.validateToken(token, customUserDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_UsernameMismatch_ReturnsFalse() {
        String token = jwtUtils.generateToken(customUserDetails);

        User diffUser = new User();
        diffUser.setEmail("other@example.com");
        diffUser.setRole(Role.USER);
        CustomUserDetails diffUserDetails = new CustomUserDetails(diffUser);

        boolean isValid = jwtUtils.validateToken(token, diffUserDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() throws Exception {
        Field expField = JwtUtils.class.getDeclaredField("EXPIRATION");
        expField.setAccessible(true);
        expField.set(jwtUtils, -1000L);

        String token = jwtUtils.generateToken(customUserDetails);

        assertThrows(Exception.class, () -> jwtUtils.validateToken(token, customUserDetails));
    }
}