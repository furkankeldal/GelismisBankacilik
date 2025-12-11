package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationConsumer {

	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "${app.kafka.transaction-topic:transaction-events}", groupId = "notification-group")
	public void consume(String message) {
		try {
			TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

			log.info("[NOTIFICATION] Yeni işlem event'i alındı: id={}, hesapNo={}, müşteriId={}, tür={}, tutar={}, öncekiBakiye={}, yeniBakiye={}, başarılı={}, tarih={}",
					event.getTransactionId(), event.getAccountNo(), event.getCustomerId(), event.getTransactionType(),
					event.getAmount(), event.getPreviousBalance(), event.getNewBalance(), event.isSuccessful(),
					event.getTransactionDate());
			log.info("[NOTIFICATION] (Simülasyon) Müşteriye SMS/MAIL gönderildi.");
		} catch (Exception e) {
			log.error("[NOTIFICATION] Mesaj parse edilemedi, raw={}", message, e);
		}
	}
}