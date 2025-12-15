package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationConsumer {

	private final ObjectMapper objectMapper;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${app.kafka.dlq-topic:transaction-events-dlq}")
	private String dlqTopic;

	@Value("${app.kafka.consumer-groups.notification-group:notification-group}")
	private String notificationGroupId;

	@Value("${app.kafka.consumer-groups.dlq-handler-group:dlq-handler-group}")
	private String dlqHandlerGroupId;

	/**
	 * Transaction event consumer
	 * Consumer Group yönetimi ile mesajlar partition'lara göre dağıtılır
	 * Aynı group ID'ye sahip consumer'lar mesajları paylaşır (load balancing)
	 */
	@KafkaListener(
		topics = "${app.kafka.transaction-topic:transaction-events}", 
		groupId = "${app.kafka.consumer-groups.notification-group:notification-group}"
	)
	public void consume(String message) {
		try {
			TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

			log.info("[NOTIFICATION] Yeni işlem event'i alındı: id={}, hesapNo={}, müşteriId={}, tür={}, tutar={}, öncekiBakiye={}, yeniBakiye={}, başarılı={}, tarih={}",
					event.getTransactionId(), event.getAccountNo(), event.getCustomerId(), event.getTransactionType(),
					event.getAmount(), event.getPreviousBalance(), event.getNewBalance(), event.isSuccessful(),
					event.getTransactionDate());
			log.info("[NOTIFICATION] (Simülasyon) Müşteriye SMS/MAIL gönderildi.");
		} catch (Exception e) {
			log.error("[NOTIFICATION] Mesaj parse edilemedi, DLQ'ya gönderiliyor, raw={}", message, e);
			// Başarısız mesajı Dead Letter Queue'ya gönder
			sendToDlq(message, e);
		}
	}

	/**
	 * Dead Letter Queue Handler
	 * Başarısız mesajları DLQ topic'ine gönderir
	 * Consumer Group yönetimi ile DLQ mesajları da dağıtılır
	 */
	@KafkaListener(
		topics = "${app.kafka.dlq-topic:transaction-events-dlq}", 
		groupId = "${app.kafka.consumer-groups.dlq-handler-group:dlq-handler-group}"
	)
	public void handleDlqMessage(String message) {
		log.error("[DLQ HANDLER] Başarısız mesaj alındı - İnceleme gerekiyor: {}", message);
		// Production'da: Alert sistemi, monitoring, admin panel bildirimi vb. eklenebilir
	}

	private void sendToDlq(String message, Exception error) {
		try {
			kafkaTemplate.send(dlqTopic, message);
			log.warn("[DLQ] Başarısız mesaj DLQ'ya gönderildi: topic={}", dlqTopic);
		} catch (Exception e) {
			log.error("[DLQ] DLQ'ya gönderme hatası", e);
		}
	}
}