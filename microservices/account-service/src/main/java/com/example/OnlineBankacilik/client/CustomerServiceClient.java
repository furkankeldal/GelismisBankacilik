package com.example.OnlineBankacilik.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.OnlineBankacilik.dto.CustomerResponseDto;

/**
 * Customer Service Feign Client
 * Doğrudan customer-service ile iletişim kurar (Eureka service discovery kullanır)
 * Resilience4j ile Circuit Breaker, Retry ve Timeout desteği
 * Resilience4j yapılandırması application.yml'de tanımlıdır
 */
@FeignClient(name = "customer-service", fallback = CustomerServiceClientFallback.class)
public interface CustomerServiceClient {

    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);

}

