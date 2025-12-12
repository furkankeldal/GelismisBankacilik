package com.example.OnlineBankacilik.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.OnlineBankacilik.dto.CustomerResponseDto;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:9017}")
public interface CustomerServiceClient {

    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);

}

