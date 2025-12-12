package com.example.OnlineBankacilik.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.OnlineBankacilik.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "processes")
public class Process {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "transaction_code", length = 50, unique = true, nullable = false)
	private String transactionCode;
	
	@ManyToOne
	@JoinColumn(name = "account_no", nullable = false)
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", length = 20, nullable = false)
	private TransactionType transactionType;

	@Column(name = "amount", precision = 19, scale = 2, nullable = false)
	private BigDecimal amount;
	
	@Column(name = "previous_balance", precision = 19, scale = 2, nullable = false)
	private BigDecimal previousBalance;
	
	@Column(name = "new_balance", precision = 19, scale = 2, nullable = false)
	private BigDecimal newBalance;
	
	@Column(name = "explanation", length = 500)
	private String explanation;
	
	@Column(name = "transaction_date", nullable = false, updatable = false)
	private LocalDateTime transactionDate;
	
	@Column(name = "successful", nullable = false)
	private boolean successful = true;

	@PrePersist
	protected void onCreate() {
		if (transactionDate == null) {
			transactionDate = LocalDateTime.now();
		}
	}
}
