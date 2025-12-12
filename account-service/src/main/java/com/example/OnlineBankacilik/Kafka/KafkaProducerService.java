package com.example.OnlineBankacilik.Kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String message) {
		log.info("PRODUCER: Kafka'ya mesaj gönderiliyor -> {}", message);
		kafkaTemplate.send("banking-events", message);
	}
}
