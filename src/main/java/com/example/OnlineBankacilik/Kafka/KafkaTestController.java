
package com.example.OnlineBankacilik.Kafka;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka-test")
public class KafkaTestController {

	private final KafkaProducerService producerService;

	public KafkaTestController(KafkaProducerService producerService) {
		this.producerService = producerService;
	}

	@PostMapping
	public String send(@RequestBody String message) {
		System.out.println(">>> CONTROLLER: İstek geldi -> " + message);
		producerService.sendMessage(message);
		return "Kafka'ya mesaj gönderildi: " + message;
	}
}