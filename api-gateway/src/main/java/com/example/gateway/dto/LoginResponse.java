package com.example.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
	private String token;
	private String type = "Bearer";
	private String username;
	private Long expiresIn; // milliseconds
}

