package com.example.OnlineBankacilik.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.OnlineBankacilik.enums.TransactionType;

import lombok.Data;

@Data
public class ProcessResponseDto {
	private Long customerId;
	private String nameSurname;
	private TransactionType transactionType;
	private BigDecimal amount;
	private BigDecimal previousBalance;
	private BigDecimal newBalance;
	private String explanation;
	private boolean succesfull;
	private java.math.BigDecimal interestRate;
	private String numberOfAccount;
	private LocalDateTime registrationDate;
}
