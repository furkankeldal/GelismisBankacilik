package com.example.OnlineBankacilik.service;

import java.util.List;

import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;

public interface AccountService {

	AccountResponseDto accountOpen(AccountRequestDto dto);

	List<AccountResponseDto> allAccounts();

	AccountResponseDto getAccount(String accountNo);

	List<AccountResponseDto> customerAccounts(Long customerId);

	void closeAccount(String accountNo);

	AccountResponseDto deposit(String accountNo, TransactionRequestDto request);

	AccountResponseDto withdraw(String accountNo, TransactionRequestDto request);

	AccountResponseDto processInterest(String accountNo);
}
