package com.motosphere.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.payment.dto.request.PaymentRequest;
import com.motosphere.payment.dto.request.RazorpayVerifyRequest;
import com.motosphere.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

	@PostMapping("/invoice/{invoiceId}")
	public ResponseEntity<?> makePayment(@PathVariable Long invoiceId, @RequestBody @Valid PaymentRequest request) {
		return ResponseEntity.ok(paymentService.makePayment(invoiceId, request));
	}

	@PostMapping("/invoice/{invoiceId}/razorpay-order")
	public ResponseEntity<?> createRazorpayOrder(@PathVariable Long invoiceId) {
		return ResponseEntity.ok(paymentService.createRazorpayOrder(invoiceId));
	}

	@PostMapping("/invoice/{invoiceId}/razorpay-verify")
	public ResponseEntity<?> verifyRazorpayPayment(@PathVariable Long invoiceId,
			@RequestBody @Valid RazorpayVerifyRequest request) {
		return ResponseEntity.ok(paymentService.verifyRazorpayPayment(invoiceId, request));
	}
}
