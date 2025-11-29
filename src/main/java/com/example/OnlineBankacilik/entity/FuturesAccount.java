package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("VADELI")
public class FuturesAccount extends Account {

	private BigDecimal interestRate;

	private Integer maturityMonth;

	private LocalDate maturityDate;

	public void interestProcessing() {
		if (interestRate != null) {
			BigDecimal increase = getAmount().multiply(interestRate);
			setAmount(getAmount().add(increase));
		}

	}

	@Override
	public void deposit(BigDecimal amount) {
		setAmount(getAmount().add(amount));

	}

	@Override
	public void withdraw(BigDecimal amount) {
		setAmount(getAmount().subtract(amount));

	}

}
