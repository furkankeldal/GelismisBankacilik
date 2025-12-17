package com.example.OnlineBankacilik.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;

/**
 * Account Service Feign Client
 * Doğrudan account-service ile iletişim kurar (Eureka service discovery kullanır)
 * Resilience4j ile Circuit Breaker, Retry ve Timeout desteği
 * Resilience4j yapılandırması application.yml'de tanımlıdır
 */
@FeignClient(name = "account-service", fallback = AccountServiceClientFallback.class)
public interface AccountServiceClient {

    @GetMapping("/accounts/{accountNo}")
    AccountResponseDto getAccount(@PathVariable("accountNo") String accountNo);

    @PostMapping("/accounts/{accountNo}/deposit")
    AccountResponseDto deposit(@PathVariable("accountNo") String accountNo, @RequestBody TransactionRequestDto request);

    @PostMapping("/accounts/{accountNo}/withdraw")
    AccountResponseDto withdraw(@PathVariable("accountNo") String accountNo, @RequestBody TransactionRequestDto request);

    @PostMapping("/accounts/{accountNo}/interest")
    AccountResponseDto processInterest(@PathVariable("accountNo") String accountNo);
}

