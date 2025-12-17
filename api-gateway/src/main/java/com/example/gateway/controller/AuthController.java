package com.example.gateway.controller;

import com.example.gateway.dto.LoginRequest;
import com.example.gateway.dto.LoginResponse;
import com.example.gateway.service.AuthService;
import com.example.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Login endpoint'i sağlar
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final JwtUtil jwtUtil;

	/**
	 * Login endpoint
	 * Kullanıcı adı ve şifre ile giriş yapar, JWT token döner
	 */
	@PostMapping("/login")
	public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
		log.info("Login request received for username: {}", request.getUsername());

		try {
			LoginResponse response = authService.login(request);
			return Mono.just(ResponseEntity.ok(response));
		} catch (RuntimeException e) {
			log.error("Login failed for username: {}", request.getUsername(), e);
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
		} catch (Exception e) {
			log.error("Unexpected error during login for username: {}", request.getUsername(), e);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		}
	}

	/**
	 * Token validation endpoint
	 * Token'ın geçerli olup olmadığını kontrol eder
	 */
	@GetMapping("/validate")
	public Mono<ResponseEntity<Map<String, Object>>> validateToken(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		log.info("Token validation request");

		Map<String, Object> response = new HashMap<>();
		
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			boolean isValid = jwtUtil.validateToken(token);
			if (isValid) {
				String username = jwtUtil.getUsernameFromToken(token);
				response.put("valid", true);
				response.put("message", "Token is valid");
				response.put("username", username);
			} else {
				response.put("valid", false);
				response.put("message", "Token is invalid or expired");
			}
		} else {
			response.put("valid", false);
			response.put("message", "Invalid token format");
		}

		return Mono.just(ResponseEntity.ok(response));
	}
}

