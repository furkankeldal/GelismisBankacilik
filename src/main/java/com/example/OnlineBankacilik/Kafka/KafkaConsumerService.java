package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

	@KafkaListener(topics = "banking-events", groupId = "banking-group")
	public void listen(String message) {
		System.out.println(">>> CONSUMER: Kafka mesajını aldım -> " + message);
	}
}