package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("VADELI")
public class FuturesAccount extends Account {

	@Column(name = "interest_rate", precision = 5, scale = 4)
	private BigDecimal interestRate;

	@Column(name = "maturity_month")
	private Integer maturityMonth;

	@Column(name = "maturity_date")
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
