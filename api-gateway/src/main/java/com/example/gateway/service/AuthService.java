package com.example.gateway.service;

import com.example.gateway.dto.LoginRequest;
import com.example.gateway.dto.LoginResponse;
import com.example.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service
 * Kullanıcı doğrulama ve token üretme işlemlerini yapar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final JwtUtil jwtUtil;

	@Value("${app.auth.default-users:admin:admin123,user:user123}")
	private String defaultUsers;

	/**
	 * Kullanıcı girişi yapar ve token döner
	 */
	public LoginResponse login(LoginRequest request) {
		log.info("Login attempt for username: {}", request.getUsername());

		// Kullanıcı doğrulama
		if (!validateUser(request.getUsername(), request.getPassword())) {
			log.warn("Invalid credentials for username: {}", request.getUsername());
			throw new RuntimeException("Invalid username or password");
		}

		// Token oluştur
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", "USER");
		String token = jwtUtil.generateToken(request.getUsername(), claims);

		log.info("Login successful for username: {}", request.getUsername());

		// Response oluştur
		LoginResponse response = new LoginResponse();
		response.setToken(token);
		response.setUsername(request.getUsername());
		response.setExpiresIn(86400000L); // 24 saat

		return response;
	}

	/**
	 * Kullanıcı doğrulama
	 * In-memory kullanıcı kontrolü
	 */
	private boolean validateUser(String username, String password) {
		// Basit in-memory kullanıcı doğrulama
		// Format: "username1:password1,username2:password2"
		if (defaultUsers == null || defaultUsers.isEmpty()) {
			// Default kullanıcılar
			return ("admin".equals(username) && "admin123".equals(password)) ||
				   ("user".equals(username) && "user123".equals(password));
		}

		String[] users = defaultUsers.split(",");
		for (String user : users) {
			String[] credentials = user.split(":");
			if (credentials.length == 2) {
				if (credentials[0].equals(username) && credentials[1].equals(password)) {
					return true;
				}
			}
		}

		return false;
	}
}

