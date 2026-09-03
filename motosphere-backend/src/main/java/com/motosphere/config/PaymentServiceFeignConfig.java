package com.motosphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

// Not @Configuration / not component-scanned on purpose - Feign instantiates
// this in a private child context per @FeignClient(configuration = ...), so
// this interceptor applies only to PaymentServiceFeignClient calls.
public class PaymentServiceFeignConfig {

	@Value("${internal.api.key}")
	private String internalApiKey;

	@Bean
	RequestInterceptor internalApiKeyInterceptor() {
		return requestTemplate -> requestTemplate.header("X-Internal-Api-Key", internalApiKey);
	}
}
