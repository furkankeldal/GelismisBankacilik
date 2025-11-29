package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VADESIZ")
public class FixedDepositAccount extends Account {

	@Override
	public void deposit(BigDecimal amount) {
		setAmount(getAmount().add(amount));

	}

	@Override
	public void withdraw(BigDecimal amount) {
		setAmount(getAmount().subtract(amount));

	}

}
