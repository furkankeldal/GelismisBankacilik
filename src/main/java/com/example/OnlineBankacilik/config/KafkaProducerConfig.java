package com.example.OnlineBankacilik.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Value("${spring.kafka.producer.retries:3}")
	private Integer retries;

	@Value("${spring.kafka.producer.retry-backoff-ms:1000}")
	private Integer retryBackoffMs;

	@Value("${spring.kafka.producer.acks:all}")
	private String acks;

	@Value("${spring.kafka.producer.max-in-flight-requests:5}")
	private Integer maxInFlightRequests;

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		
		// Temel Kafka ayarları
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		
		// Retry Policy Ayarları
		config.put(ProducerConfig.RETRIES_CONFIG, retries);
		config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
		
		// Mesaj Güvenilirliği
		config.put(ProducerConfig.ACKS_CONFIG, acks);
		
		// Performans Ayarları
		config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequests);
		
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}

