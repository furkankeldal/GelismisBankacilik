package com.example.gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.gateway.dto.LoginRequest;
import com.example.gateway.dto.LoginResponse;
import com.example.gateway.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Unit Tests")
class AuthServiceTest {

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService authService;

	private LoginRequest testLoginRequest;
	private String testToken;

	@BeforeEach
	void setUp() {
		testLoginRequest = new LoginRequest();
		testLoginRequest.setUsername("admin");
		testLoginRequest.setPassword("admin123");

		testToken = "test-jwt-token-12345";
	}

	@Test
	@DisplayName("Should login successfully with valid credentials")
	void testLogin_Success() {
		// Given
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", "USER");
		
		when(jwtUtil.generateToken(eq("admin"), any(Map.class))).thenReturn(testToken);

		// When
		LoginResponse result = authService.login(testLoginRequest);

		// Then
		assertNotNull(result);
		assertEquals(testToken, result.getToken());
		assertEquals("admin", result.getUsername());
		assertEquals(86400000L, result.getExpiresIn());
		verify(jwtUtil, times(1)).generateToken(eq("admin"), any(Map.class));
	}

	@Test
	@DisplayName("Should throw exception for invalid credentials")
	void testLogin_InvalidCredentials() {
		// Given
		LoginRequest invalidRequest = new LoginRequest();
		invalidRequest.setUsername("invalid");
		invalidRequest.setPassword("wrongpassword");

		// When & Then
		assertThrows(RuntimeException.class, () -> authService.login(invalidRequest));
		verify(jwtUtil, never()).generateToken(anyString(), any(Map.class));
	}

	@Test
	@DisplayName("Should login with default users when defaultUsers is null")
	void testLogin_DefaultUsers_Null() {
		// Given
		ReflectionTestUtils.setField(authService, "defaultUsers", null);
		when(jwtUtil.generateToken(eq("admin"), any(Map.class))).thenReturn(testToken);

		// When
		LoginResponse result = authService.login(testLoginRequest);

		// Then
		assertNotNull(result);
		assertEquals(testToken, result.getToken());
		verify(jwtUtil, times(1)).generateToken(eq("admin"), any(Map.class));
	}

	@Test
	@DisplayName("Should login with default users when defaultUsers is empty")
	void testLogin_DefaultUsers_Empty() {
		// Given
		ReflectionTestUtils.setField(authService, "defaultUsers", "");
		when(jwtUtil.generateToken(eq("admin"), any(Map.class))).thenReturn(testToken);

		// When
		LoginResponse result = authService.login(testLoginRequest);

		// Then
		assertNotNull(result);
		assertEquals(testToken, result.getToken());
		verify(jwtUtil, times(1)).generateToken(eq("admin"), any(Map.class));
	}

	@Test
	@DisplayName("Should login with configured users")
	void testLogin_ConfiguredUsers() {
		// Given
		ReflectionTestUtils.setField(authService, "defaultUsers", "testuser:testpass,admin:admin123");
		LoginRequest configuredRequest = new LoginRequest();
		configuredRequest.setUsername("testuser");
		configuredRequest.setPassword("testpass");
		
		when(jwtUtil.generateToken(eq("testuser"), any(Map.class))).thenReturn(testToken);

		// When
		LoginResponse result = authService.login(configuredRequest);

		// Then
		assertNotNull(result);
		assertEquals(testToken, result.getToken());
		assertEquals("testuser", result.getUsername());
		verify(jwtUtil, times(1)).generateToken(eq("testuser"), any(Map.class));
	}

	@Test
	@DisplayName("Should login with user credentials")
	void testLogin_UserCredentials() {
		// Given
		LoginRequest userRequest = new LoginRequest();
		userRequest.setUsername("user");
		userRequest.setPassword("user123");
		
		when(jwtUtil.generateToken(eq("user"), any(Map.class))).thenReturn(testToken);

		// When
		LoginResponse result = authService.login(userRequest);

		// Then
		assertNotNull(result);
		assertEquals(testToken, result.getToken());
		assertEquals("user", result.getUsername());
		verify(jwtUtil, times(1)).generateToken(eq("user"), any(Map.class));
	}
}

