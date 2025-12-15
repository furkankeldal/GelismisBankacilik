package com.example.OnlineBankacilik.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessRequestDto {

	@NotBlank
	private String accountNo;
	@NotNull
	@DecimalMin(value = "0.0", inclusive = false)
	private BigDecimal amount;
	private String explanation;
}

