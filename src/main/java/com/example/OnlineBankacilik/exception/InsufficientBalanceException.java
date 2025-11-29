package com.example.OnlineBankacilik.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException(BigDecimal desired, BigDecimal available) {
		super("Bakiye yetersiz.Mevcut: " + available + ", istenen: " + desired);
	}

}
