package com.example.OnlineBankacilik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OnlineBankacilikApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineBankacilikApplication.class, args);
	}

}
