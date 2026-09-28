package com.microlend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    // 256-bit secret key (Base64 encoded)
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
        jwtService.init();
    }

    @Test
    void testInit_InvalidSecret_ThrowsException() {
        JwtService invalidService = new JwtService();
        ReflectionTestUtils.setField(invalidService, "secretKey", "");
        ReflectionTestUtils.setField(invalidService, "jwtExpiration", 1000L);
        assertThrows(IllegalArgumentException.class, invalidService::init);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);
        
        assertNotNull(token);
        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void testIsTokenValid() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);
        
        assertTrue(jwtService.isTokenValid(token, userDetails));
        
        UserDetails otherUser = new User("other@example.com", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secretKey", testSecret);
        ReflectionTestUtils.setField(shortLivedService, "jwtExpiration", 1L); // 1 ms expiration
        shortLivedService.init();

        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = shortLivedService.generateToken(userDetails);
        
        Thread.sleep(5); // Wait for token to expire

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            shortLivedService.extractUsername(token);
        });
    }
}
