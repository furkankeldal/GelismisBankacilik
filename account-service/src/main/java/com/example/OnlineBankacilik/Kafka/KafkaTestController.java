package com.example.OnlineBankacilik.Kafka;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/kafka-test")
@RequiredArgsConstructor
public class KafkaTestController {

	private final KafkaProducerService producerService;

	@PostMapping
	public String send(@RequestBody String message) {
		log.info("CONTROLLER: Kafka test mesajı gönderiliyor -> {}", message);
		producerService.sendMessage(message);
		return "Kafka'ya mesaj gönderildi: " + message;
	}
}