package com.example.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Filter (Custom implementation)
 * Spring Cloud Gateway'in built-in RequestRateLimiter filter'ı route'larda kullanılıyor.
 * Bu filter sadece rate limit header'larını ve log'ları yönetir.
 */
@Component
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

	@Value("${app.gateway.rate-limit.enabled:true}")
	private boolean rateLimitEnabled;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (!rateLimitEnabled) {
			return chain.filter(exchange);
		}

		// Rate limit bilgilerini logla
		ServerHttpResponse response = exchange.getResponse();
		
		return chain.filter(exchange).doFinally(signalType -> {
			String remaining = response.getHeaders().getFirst("X-RateLimit-Remaining");
			String limit = response.getHeaders().getFirst("X-RateLimit-Limit");
			if (remaining != null && limit != null) {
				log.debug("Rate limit - Remaining: {}/{}, Client: {}", 
					remaining, limit, exchange.getRequest().getRemoteAddress());
			}
		});
	}

	@Override
	public int getOrder() {
		return -50; // Authentication'dan sonra
	}
}

