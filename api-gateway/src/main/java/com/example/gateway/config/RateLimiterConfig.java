package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

/**
 * Rate Limiter Configuration
 */
@Configuration
public class RateLimiterConfig {

	/**
	 * IP-based rate limiting key resolver
	 * Primary KeyResolver for RequestRateLimiter filter
	 */
	@Bean
	@Primary
	public KeyResolver ipKeyResolver() {
		return exchange -> {
			String clientIp = exchange.getRequest().getRemoteAddress() != null
				? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
				: "unknown";
			return Mono.just(clientIp);
		};
	}

	/**
	 * API Key-based rate limiting key resolver (alternatif)
	 */
	@Bean
	public KeyResolver apiKeyResolver() {
		return exchange -> {
			String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
			if (apiKey != null && !apiKey.isEmpty()) {
				return Mono.just(apiKey);
			}
			// Fallback to IP if no API key
			String clientIp = exchange.getRequest().getRemoteAddress() != null
				? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
				: "unknown";
			return Mono.just(clientIp);
		};
	}
}

