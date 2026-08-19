package com.motosphere.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CoreServiceClientConfig {

	@Value("${core.service.base-url}")
	private String coreServiceBaseUrl;

	@Bean
	WebClient coreServiceWebClient() {
		return WebClient.builder().baseUrl(coreServiceBaseUrl).build();
	}
}
