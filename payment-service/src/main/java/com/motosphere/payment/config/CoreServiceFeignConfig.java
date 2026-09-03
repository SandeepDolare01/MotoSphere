package com.motosphere.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

// Feign "configuration" classes are intentionally NOT annotated with
// @Configuration and are NOT picked up by component-scanning - they're
// instantiated in a private child context per @FeignClient(configuration = ...)
// so this interceptor only ever applies to CoreServiceFeignClient calls, not
// globally to every Feign client in the app.
public class CoreServiceFeignConfig {

	@Value("${internal.api.key}")
	private String internalApiKey;

	@Bean
	RequestInterceptor internalApiKeyInterceptor() {
		return requestTemplate -> requestTemplate.header("X-Internal-Api-Key", internalApiKey);
	}
}
