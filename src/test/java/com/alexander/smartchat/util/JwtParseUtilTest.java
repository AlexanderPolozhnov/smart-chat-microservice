package com.alexander.smartchat.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtParseUtilTest {

    @Test
    public void testGetTokenFromRequest_withValidToken() {
        String bearerToken = "Bearer validToken";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);

        String token = JwtParseUtil.getTokenFromRequest(request);

        assertNotNull(token);
        assertEquals("validToken", token);
    }

    @Test
    public void testGetTokenFromRequest_withNoToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        String token = JwtParseUtil.getTokenFromRequest(request);

        assertNull(token);
    }

    @Test
    public void testGetTokenFromRequest_withInvalidToken() {
        String bearerToken = "InvalidToken";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);

        String token = JwtParseUtil.getTokenFromRequest(request);

        assertNull(token);
    }

    @Test
    public void testGetTokenFromRequest_withEmptyToken() {
        String bearerToken = "Bearer ";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);

        String token = JwtParseUtil.getTokenFromRequest(request);

        assertTrue(token.isEmpty());
    }
}
