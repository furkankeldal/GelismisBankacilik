package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaConsumerService {

	@KafkaListener(topics = "banking-events", groupId = "banking-group")
	public void listen(String message) {
		log.info("[KAFKA CONSUMER] Banking event mesajı alındı: {}", message);
	}
}