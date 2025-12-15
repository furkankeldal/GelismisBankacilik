package com.example.OnlineBankacilik.exception;

public class AccountNotFoundException extends RuntimeException {
	public AccountNotFoundException(String accountNo) {
		super("hesap bulunamadı: " + accountNo);
	}

}

