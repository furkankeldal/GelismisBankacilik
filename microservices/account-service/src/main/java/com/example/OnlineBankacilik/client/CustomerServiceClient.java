package com.example.OnlineBankacilik.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.OnlineBankacilik.dto.CustomerResponseDto;

/**
 * Customer Service Feign Client
 * Doğrudan customer-service ile iletişim kurar (Eureka service discovery kullanır)
 */
@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);

}

