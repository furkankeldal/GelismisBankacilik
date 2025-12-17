package com.example.OnlineBankacilik.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisConfig {

	// Spring Boot'un otomatik yapılandırdığı ObjectMapper bean'ini kullan
	// Bu, Spring Boot'un tüm otomatik yapılandırmalarını içerir (JavaTimeModule dahil)
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory connectionFactory,
			ObjectMapper objectMapper) {
		log.info("RedisTemplate oluşturuluyor...");
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.afterPropertiesSet();
		log.info("RedisTemplate başarıyla oluşturuldu (Spring Boot ObjectMapper ile)");
		return template;
	}
	
	@Bean
	public CacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			ObjectMapper objectMapper) {
		log.info("RedisCacheManager oluşturuluyor...");
		
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		
		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofHours(1))
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
				.disableCachingNullValues();
		
		RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(cacheConfig)
				.transactionAware()
				.build();
		
		log.info("RedisCacheManager başarıyla oluşturuldu (Spring Boot ObjectMapper ile)");
		return cacheManager;
	}
}

