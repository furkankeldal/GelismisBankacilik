package com.example.OnlineBankacilik.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.OnlineBankacilik.client")
public class FeignConfig {

}

