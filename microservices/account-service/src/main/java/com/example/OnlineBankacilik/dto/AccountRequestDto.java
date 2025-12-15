package com.example.OnlineBankacilik.dto;

import java.math.BigDecimal;

import com.example.OnlineBankacilik.enums.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequestDto {

	@NotNull
	private Long customerId;
	@NotNull
	private AccountType accountType;
	@NotNull
	@DecimalMin(value = "0.0", inclusive = false)
	private BigDecimal firstAmount;

	// only for VADELI
	private BigDecimal interestRate;
	private Integer maturityMonth;
}
