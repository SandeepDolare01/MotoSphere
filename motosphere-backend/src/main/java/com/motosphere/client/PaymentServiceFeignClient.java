package com.motosphere.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.motosphere.config.PaymentServiceFeignConfig;
import com.motosphere.dto.response.AdminDashboardResponse;
import com.motosphere.dto.response.ManagerDashboardResponse;

// Declarative replacement for the old WebClient bean in
// the removed PaymentServiceClientConfig. "url" pins this at payment-service's base URL
// from application.properties (no service discovery in this setup). The
// X-Internal-Api-Key header is added automatically by
// PaymentServiceFeignConfig's RequestInterceptor, not declared per-method.
@FeignClient(name = "payment-service", url = "${payment.service.base-url}", configuration = PaymentServiceFeignConfig.class)
public interface PaymentServiceFeignClient {

	@GetMapping("/internal/payments/admin-summary")
	AdminDashboardResponse getAdminSummary();

	@GetMapping("/internal/payments/garage-summary/{garageId}")
	ManagerDashboardResponse getGarageSummary(@PathVariable("garageId") Long garageId);
}
