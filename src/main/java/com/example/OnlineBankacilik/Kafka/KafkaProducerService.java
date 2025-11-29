package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage(String message) {
		System.out.println(">>> PRODUCER: Kafka'ya mesaj gönderiyorum -> " + message);
		kafkaTemplate.send("banking-events", message);
	}
}
