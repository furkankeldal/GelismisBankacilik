package com.example.OnlineBankacilik.client;

import org.springframework.stereotype.Component;

import com.example.OnlineBankacilik.dto.CustomerResponseDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Customer Service Client Fallback
 * Customer Service down olduğunda veya hata durumunda çağrılır
 */
@Slf4j
@Component
public class CustomerServiceClientFallback implements CustomerServiceClient {

	@Override
	public CustomerResponseDto getCustomerById(Long id) {
		log.error("Customer Service fallback triggered for customerId: {}", id);
		throw new RuntimeException("Customer Service geçici olarak kullanılamıyor. Müşteri bilgisi alınamadı. Lütfen daha sonra tekrar deneyin.");
	}
}

