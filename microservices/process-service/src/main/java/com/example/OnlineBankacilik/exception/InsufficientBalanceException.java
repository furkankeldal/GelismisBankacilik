package com.example.OnlineBankacilik.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException(BigDecimal available, BigDecimal desired) {
		super("Bakiye yetersiz.Mevcut: " + available + ", istenen: " + desired);
	}

}

