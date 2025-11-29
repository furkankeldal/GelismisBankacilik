package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.entity.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionNotificationConsumer {

	private final ObjectMapper objectMapper;

	public TransactionNotificationConsumer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${app.kafka.transaction-topic:transaction-events}", groupId = "notification-group")
	public void consume(String message) {
		try {
			TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

			System.out.println(">>> [NOTIFICATION] Yeni işlem event'i alındı:");
			System.out.println("    İşlem ID     : " + event.getTransactionId());
			System.out.println("    Hesap No     : " + event.getAccountNo());
			System.out.println("    Müşteri ID   : " + event.getCustomerId());
			System.out.println("    Tür          : " + event.getTransactionType());
			System.out.println("    Tutar        : " + event.getAmount());
			System.out.println("    Önceki Bakiye: " + event.getPreviousBalance());
			System.out.println("    Yeni Bakiye  : " + event.getNewBalance());
			System.out.println("    Başarılı mı  : " + event.isSuccessful());
			System.out.println("    Tarih        : " + event.getTransactionDate());
			System.out.println(">>> [NOTIFICATION] (Simülasyon) Müşteriye SMS/MAIL gönderildi.");
		} catch (Exception e) {
			System.out.println(">>> [NOTIFICATION] Mesaj parse edilemedi, raw: " + message);
		}
	}
}