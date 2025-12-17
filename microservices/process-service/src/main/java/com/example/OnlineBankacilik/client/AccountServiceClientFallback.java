package com.example.OnlineBankacilik.client;

import org.springframework.stereotype.Component;

import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Account Service Client Fallback
 * Account Service down olduğunda veya hata durumunda çağrılır
 */
@Slf4j
@Component
public class AccountServiceClientFallback implements AccountServiceClient {

	@Override
	public AccountResponseDto getAccount(String accountNo) {
		log.error("Account Service fallback triggered for accountNo: {}", accountNo);
		throw new AccountNotFoundException("Account Service geçici olarak kullanılamıyor. Lütfen daha sonra tekrar deneyin.");
	}

	@Override
	public AccountResponseDto deposit(String accountNo, TransactionRequestDto request) {
		log.error("Account Service fallback triggered for deposit - accountNo: {}", accountNo);
		throw new RuntimeException("Account Service geçici olarak kullanılamıyor. Para yatırma işlemi gerçekleştirilemedi.");
	}

	@Override
	public AccountResponseDto withdraw(String accountNo, TransactionRequestDto request) {
		log.error("Account Service fallback triggered for withdraw - accountNo: {}", accountNo);
		throw new RuntimeException("Account Service geçici olarak kullanılamıyor. Para çekme işlemi gerçekleştirilemedi.");
	}

	@Override
	public AccountResponseDto processInterest(String accountNo) {
		log.error("Account Service fallback triggered for processInterest - accountNo: {}", accountNo);
		throw new RuntimeException("Account Service geçici olarak kullanılamıyor. Faiz işlemi gerçekleştirilemedi.");
	}
}

