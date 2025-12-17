package com.example.gateway.filter;

import com.example.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationFilter Unit Tests")
class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        // Varsayılan ayarlar
        ReflectionTestUtils.setField(authenticationFilter, "authEnabled", true);
        ReflectionTestUtils.setField(authenticationFilter, "apiKeyHeader", "X-API-Key");
        ReflectionTestUtils.setField(authenticationFilter, "jwtHeader", HttpHeaders.AUTHORIZATION);
        ReflectionTestUtils.setField(authenticationFilter, "publicPaths", new String[]{"/api/auth", "/actuator"});
    }

    @Test
    @DisplayName("Public path için authentication bypass edilmeli")
    void testPublicPath_BypassesAuthentication() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Auth disabled olduğunda tüm istekler izinli olmalı")
    void testAuthDisabled_AllowsRequest() {
        ReflectionTestUtils.setField(authenticationFilter, "authEnabled", false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/customers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Geçerli JWT token ile istek izinli olmalı")
    void testValidJwt_AllowsRequest() {
        String token = "valid-jwt-token";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("admin");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        verify(jwtUtil, times(1)).validateToken(token);
        verify(jwtUtil, times(1)).getUsernameFromToken(token);
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("Geçersiz JWT token ile 401 dönmeli")
    void testInvalidJwt_ReturnsUnauthorized() {
        String token = "invalid-jwt-token";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(token)).thenReturn(false);

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil, times(1)).validateToken(token);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("API key varsa istek izinli olmalı")
    void testApiKey_AllowsRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/customers")
                .header("X-API-Key", "test-api-key")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Ne JWT ne API key yoksa 401 dönmeli")
    void testNoAuthHeaders_ReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/customers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        authenticationFilter.filter(exchange, chain).block(Duration.ofSeconds(1));

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
        verifyNoInteractions(jwtUtil);
    }
}


