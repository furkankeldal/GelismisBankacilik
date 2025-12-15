package com.example.OnlineBankacilik.exception;

public class InvalidAmountException extends RuntimeException {
	public InvalidAmountException() {
		super("Miktar 0'dan büyük olmalıdır");
	}

}

