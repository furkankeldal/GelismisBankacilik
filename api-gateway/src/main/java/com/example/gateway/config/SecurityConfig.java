package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Configuration
 * CSRF korumasını devre dışı bırakır (API Gateway için)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
			.csrf(csrf -> csrf.disable()) // CSRF korumasını devre dışı bırak
			.authorizeExchange(exchanges -> exchanges
				.anyExchange().permitAll() // Tüm endpoint'lere izin ver (AuthenticationFilter zaten kontrol ediyor)
			);

		return http.build();
	}
}

