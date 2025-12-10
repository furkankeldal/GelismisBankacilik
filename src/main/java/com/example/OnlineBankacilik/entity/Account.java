package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", length = 20)
public abstract class Account {
	
	@Id
	@Column(name = "account_no", length = 50, unique = true, nullable = false)
	private String accountNo;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "amount", precision = 19, scale = 2, nullable = false)
	private BigDecimal amount = BigDecimal.ZERO;
	
	@Column(name = "opening_date", nullable = false)
	private LocalDateTime openingDate;
	
	@Column(name = "active", nullable = false)
	private boolean active = true;

	@PrePersist
	protected void onCreate() {
		if (openingDate == null) {
			openingDate = LocalDateTime.now();
		}
	}

	public abstract void deposit(BigDecimal amount);

	public abstract void withdraw(BigDecimal amount);
}
