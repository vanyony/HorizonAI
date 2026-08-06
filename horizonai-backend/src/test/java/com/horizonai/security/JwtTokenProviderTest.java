package com.horizonai.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            "horizonai-test-secret-key-must-be-at-least-256-bits-long",
            60_000L
    );

    @Test
    void generatedTokenRoundTripsIdentityClaims() {
        String token = provider.generateToken(42L, "alice", "USER");

        assertTrue(provider.validateToken(token));
        assertEquals(42L, provider.getUserIdFromToken(token));
        assertEquals("alice", provider.getUsernameFromToken(token));
        assertEquals("USER", provider.getRoleFromToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generateToken(42L, "alice", "USER");
        String tampered = token.substring(0, token.length() - 1) + "x";

        assertFalse(provider.validateToken(tampered));
    }
}
