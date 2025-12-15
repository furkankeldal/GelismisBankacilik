package com.example.OnlineBankacilik.exception;

public class CustomerNotFoundException extends RuntimeException {
	public CustomerNotFoundException(Long id) {
		super("Müşteri bulunamadı: " + id);
	}

	public CustomerNotFoundException(String message) {
		super("Müşteri bulunamadı:" + message);
	}

}

