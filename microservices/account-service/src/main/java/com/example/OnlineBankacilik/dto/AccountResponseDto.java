package com.example.OnlineBankacilik.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.OnlineBankacilik.enums.AccountType;

import lombok.Data;

@Data
public class AccountResponseDto {

	private String accountNo;
	private Long customerId;

	private AccountType accountType;
	private BigDecimal amount;
	private LocalDateTime openingDate;
	private boolean active;

	// optional VADELI extras
	private BigDecimal interestRate;
	private Integer maturityMonth;
	private LocalDate maturityDate;
}
