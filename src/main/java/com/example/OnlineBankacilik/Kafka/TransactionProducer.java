package com.example.OnlineBankacilik.Kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.entity.TransactionEvent;
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
			log.info("Kafka transaction event gönderiliyor: {}", json);
			kafkaTemplate.send(transactionTopic, json);
		} catch (JsonProcessingException e) {
			log.error("Kafka event serileştirme hatası", e);
		}
	}
}