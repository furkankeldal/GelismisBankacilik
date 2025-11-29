package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.OnlineBankacilik.enums.TransactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Process {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String transactionCode; // e.g., TXN-001
	@ManyToOne
	@JoinColumn(name = "account_no")
	private Account account;

	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	private BigDecimal amount;
	private BigDecimal previousBalance;
	private BigDecimal newBalance;
	private String explanation;
	private LocalDateTime transactionDate = LocalDateTime.now();
	private boolean successful = true;

}
