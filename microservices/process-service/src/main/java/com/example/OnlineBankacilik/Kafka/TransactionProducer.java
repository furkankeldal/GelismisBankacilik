package com.example.OnlineBankacilik.Kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${app.kafka.transaction-topic:transaction-events}")
	private String transactionTopic;

	public void publish(TransactionEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			// Key olarak accountNo kullan (aynı hesap için aynı partition'a gitsin - ordering garantisi)
			String key = event.getAccountNo();
			log.info("Kafka transaction event gönderiliyor: key={}, event={}", key, json);
			kafkaTemplate.send(transactionTopic, key, json);
		} catch (JsonProcessingException e) {
			log.error("Kafka event serileştirme hatası: accountNo={}", event.getAccountNo(), e);
			throw new RuntimeException("Kafka event gönderilemedi", e);
		} catch (Exception e) {
			log.error("Kafka event gönderme hatası: accountNo={}", event.getAccountNo(), e);
			// Exception fırlatma, işlem başarılı olsa da event gönderilemese sorun olmasın
			// Production'da Dead Letter Queue (DLQ) kullanılabilir
		}
	}
}

