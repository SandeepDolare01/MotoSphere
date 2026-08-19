package com.motosphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentServiceClientConfig {

	@Value("${payment.service.base-url}")
	private String paymentServiceBaseUrl;

	@Bean
	WebClient paymentServiceWebClient() {
		return WebClient.builder().baseUrl(paymentServiceBaseUrl).build();
	}
}
