package com.example.OnlineBankacilik.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private String transactionId; // Process.transactionCode
	private String accountNo; // İşlemin yapıldığı hesap numarası
	private Long customerId; // İşlemi yapan müşteri ID
	private com.example.OnlineBankacilik.enums.TransactionType transactionType; // YATIRMA / CEKME
	private BigDecimal amount; // İşlem tutarı
	private BigDecimal previousBalance; // Eski bakiye
	private BigDecimal newBalance; // Yeni bakiye
	private boolean successful; // İşlem başarılı mı?
	private LocalDateTime transactionDate; // Zaman damgası
}