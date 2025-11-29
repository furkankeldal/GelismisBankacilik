package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Account_type")
public abstract class Account {
	@Id
	private String accountNo;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	private BigDecimal amount = BigDecimal.ZERO;
	private LocalDateTime openingDate;
	private boolean active;

	public abstract void deposit(BigDecimal amount);

	public abstract void withdraw(BigDecimal amount);

}
