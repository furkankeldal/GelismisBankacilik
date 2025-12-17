package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request/Response Logging Filter
 * Tüm istekleri ve yanıtları loglar, distributed tracing için trace ID ekler
 */
@Component
@Slf4j
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

	private static final String TRACE_ID_HEADER = "X-Trace-Id";
	private static final String START_TIME_ATTR = "startTime";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String traceId = generateTraceId(request);
		final long startTime = System.currentTimeMillis(); // final for lambda access

		// Trace ID'yi header'a ve exchange attribute'a ekle
		ServerHttpRequest modifiedRequest = request.mutate()
			.header(TRACE_ID_HEADER, traceId)
			.build();

		exchange.getAttributes().put(START_TIME_ATTR, startTime);
		exchange.getAttributes().put("traceId", traceId);

		// Request logging
		logRequest(modifiedRequest, traceId);

		// Request body'yi loglamak için
		if (hasBody(modifiedRequest)) {
			return DataBufferUtils.join(exchange.getRequest().getBody())
				.flatMap(dataBuffer -> {
					byte[] bytes = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(bytes);
					DataBufferUtils.release(dataBuffer);
					String body = new String(bytes, StandardCharsets.UTF_8);
					log.info("[REQUEST BODY] TraceId: {}, Body: {}", traceId, body);

					ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(modifiedRequest) {
						@Override
						public Flux<DataBuffer> getBody() {
							return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
						}
					};

					return chain.filter(exchange.mutate().request(decoratedRequest).build())
						.then(Mono.fromRunnable(() -> logResponse(exchange, traceId, startTime)));
				});
		}

		// Response logging
		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				// Not: Header ekleme ReadOnlyHttpHeaders sorunu nedeniyle kaldırıldı
				// Header'lar sadece log'da gösteriliyor
				
				if (body instanceof Flux) {
					Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
					return super.writeWith(
						fluxBody
							.map(dataBuffer -> {
								byte[] bytes = new byte[dataBuffer.readableByteCount()];
								dataBuffer.read(bytes);
								String responseBody = new String(bytes, StandardCharsets.UTF_8);
								logResponse(exchange, traceId, startTime, responseBody);
								// Yeni DataBuffer oluştur (orijinal release edilmiş olabilir)
								return exchange.getResponse().bufferFactory().wrap(bytes);
							})
					);
				}
				return super.writeWith(body);
			}
		};

		return chain.filter(exchange.mutate()
			.request(modifiedRequest)
			.response(decoratedResponse)
			.build())
			.then(Mono.fromRunnable(() -> logResponse(exchange, traceId, startTime)));
	}

	private void logRequest(ServerHttpRequest request, String traceId) {
		log.info("[REQUEST] TraceId: {}, Method: {}, Path: {}, Headers: {}, QueryParams: {}, RemoteAddress: {}",
			traceId,
			request.getMethod(),
			request.getURI().getPath(),
			request.getHeaders(),
			request.getQueryParams(),
			request.getRemoteAddress()
		);
	}

	private void logResponse(ServerWebExchange exchange, String traceId, long startTime) {
		logResponse(exchange, traceId, startTime, null);
	}

	private void logResponse(ServerWebExchange exchange, String traceId, long startTime, String responseBody) {
		long duration = System.currentTimeMillis() - startTime;
		ServerHttpResponse response = exchange.getResponse();

		String logMessage = "[RESPONSE] TraceId: {}, Status: {}, Duration: {}ms, Headers: {}";
		if (responseBody != null && !responseBody.isEmpty()) {
			logMessage += ", Body: " + (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
		}

		log.info(logMessage,
			traceId,
			response.getStatusCode(),
			duration,
			response.getHeaders()
		);

		// Not: Header'lar ServerHttpResponseDecorator içinde ekleniyor (ReadOnlyHttpHeaders sorunu için)
	}

	private boolean hasBody(ServerHttpRequest request) {
		return request.getHeaders().getContentLength() > 0;
	}

	private String generateTraceId(ServerHttpRequest request) {
		String existingTraceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
		if (existingTraceId != null && !existingTraceId.isEmpty()) {
			return existingTraceId;
		}
		return UUID.randomUUID().toString();
	}

	@Override
	public int getOrder() {
		return -200; // En düşük order - ilk çalışacak
	}
}

