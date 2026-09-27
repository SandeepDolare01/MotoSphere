package com.motosphere.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.payment.service.PaymentService;
import com.motosphere.payment.util.InternalApiKeyGuard;

import lombok.RequiredArgsConstructor;

// Called by motosphere-backend's DashboardServiceImpl, never by a browser -
// see InternalApiKeyGuard for how this is protected instead of the usual
// JWT/role checks.
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalController {
	private final PaymentService paymentService;
	private final InternalApiKeyGuard apiKeyGuard;

	@GetMapping("/admin-summary")
	public ResponseEntity<?> getAdminSummary(@RequestHeader("X-Internal-Api-Key") String apiKey) {
		apiKeyGuard.verify(apiKey);
		return ResponseEntity.ok(paymentService.getAdminSummary());
	}

	@GetMapping("/garage-summary/{garageId}")
	public ResponseEntity<?> getGarageSummary(@PathVariable Long garageId,
			@RequestHeader("X-Internal-Api-Key") String apiKey) {
		apiKeyGuard.verify(apiKey);
		return ResponseEntity.ok(paymentService.getGarageSummary(garageId));
	}
}
