package com.example.gateway.filter;

import com.example.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Authentication Filter
 * JWT token validation ve API key kontrolü yapar
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtUtil jwtUtil;

	@Value("${app.gateway.auth.enabled:true}")
	private boolean authEnabled;

	@Value("${app.gateway.auth.api-key-header:X-API-Key}")
	private String apiKeyHeader;

	@Value("${app.gateway.auth.jwt-header:Authorization}")
	private String jwtHeader;

	@Value("${app.gateway.auth.public-paths:}")
	private String[] publicPaths;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();

		// Public path kontrolü (en önce kontrol et)
		if (isPublicPath(path)) {
			log.info("Public path - Authentication bypassed: {}", path);
			return chain.filter(exchange);
		}

		// Auth disabled ise bypass
		if (!authEnabled) {
			log.debug("Authentication disabled - Request allowed: {}", path);
			return chain.filter(exchange);
		}

		// JWT Token kontrolü
		String authHeader = request.getHeaders().getFirst(jwtHeader);
		String apiKey = request.getHeaders().getFirst(apiKeyHeader);

		if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			// JWT token validation
			try {
				if (jwtUtil.validateToken(token)) {
					String username = jwtUtil.getUsernameFromToken(token);
					log.debug("JWT token validated - Username: {}, Path: {}", username, path);
			return chain.filter(exchange);
				} else {
					log.warn("Invalid JWT token - Path: {}", path);
					return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
				}
			} catch (Exception e) {
				log.error("Error validating JWT token - Path: {}", path, e);
				return onError(exchange, "Token validation error", HttpStatus.UNAUTHORIZED);
			}
		}

		// API Key kontrolü
		if (StringUtils.hasText(apiKey)) {
			// TODO: API key validation (Redis veya DB'den kontrol edilebilir)
			log.debug("API key found - Request allowed: {}", path);
			return chain.filter(exchange);
		}

		// Authentication başarısız
		log.warn("Authentication failed - No valid token or API key: {}", path);
		return onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED);
	}

	private boolean isPublicPath(String path) {
		if (publicPaths == null || publicPaths.length == 0) {
			log.debug("No public paths configured");
			return false;
		}
		
		// /actuator için özel kontrol: path içinde /actuator varsa public kabul et
		// Bu sayede /customer-service/actuator/health gibi path'ler de çalışır
		// Eureka discovery locator ile oluşan path'ler için: /{service-name}/actuator/**
		if (path.contains("/actuator")) {
			log.info("Actuator path detected as public: {}", path);
			return true;
		}
		
		for (String publicPath : publicPaths) {
			// Exact match veya starts with kontrolü
			if (path.startsWith(publicPath)) {
				log.debug("Public path matched (startsWith): {} -> {}", path, publicPath);
				return true;
			}
		}
		
		log.debug("Path is not public: {}", path);
		return false;
	}

	private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().add("Content-Type", "application/json");
		return response.writeWith(
			Mono.just(response.bufferFactory().wrap(
				("{\"error\":\"" + message + "\",\"status\":" + status.value() + "}").getBytes()
			))
		);
	}

	@Override
	public int getOrder() {
		return -100; // High priority
	}
}

