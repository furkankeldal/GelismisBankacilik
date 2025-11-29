package com.example.OnlineBankacilik.Kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.OnlineBankacilik.entity.TransactionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${app.kafka.transaction-topic:transaction-events}")
	private String transactionTopic;

	public TransactionProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void publish(TransactionEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			System.out.println(">>> KAFKA TRANSACTION EVENT GÖNDERİLİYOR: " + json);
			kafkaTemplate.send(transactionTopic, json);
		} catch (JsonProcessingException e) {
			System.err.println("Kafka event serileştirme hatası: " + e.getMessage());
		}
	}
}