package com.motosphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.response.ApiResponse;
import com.motosphere.service.InvoiceService;
import com.motosphere.util.InternalApiKeyGuard;

import lombok.RequiredArgsConstructor;

// Everything here is called by payment-service, never by a browser - see
// InternalApiKeyGuard for how these are protected instead of the usual
// JWT/role checks.
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {
	private final InvoiceService invoiceService;
	private final InternalApiKeyGuard apiKeyGuard;

	@GetMapping("/invoices/{invoiceId}")
	public ResponseEntity<?> getInvoiceDetails(@PathVariable Long invoiceId,
			@RequestHeader("X-Internal-Api-Key") String apiKey) {
		apiKeyGuard.verify(apiKey);
		return ResponseEntity.ok(invoiceService.getInvoiceDetailsForInternal(invoiceId));
	}

	@PatchMapping("/invoices/{invoiceId}/mark-paid")
	public ResponseEntity<?> markInvoicePaid(@PathVariable Long invoiceId,
			@RequestHeader("X-Internal-Api-Key") String apiKey) {
		apiKeyGuard.verify(apiKey);
		invoiceService.markInvoicePaidInternal(invoiceId);
		return ResponseEntity.ok(new ApiResponse("Invoice marked paid", "Success"));
	}
}
