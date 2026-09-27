package com.motosphere.payment.service;

import com.motosphere.payment.dto.request.PaymentRequest;
import com.motosphere.payment.dto.request.RazorpayVerifyRequest;
import com.motosphere.payment.dto.response.AdminDashboardResponse;
import com.motosphere.payment.dto.response.ManagerDashboardResponse;
import com.motosphere.payment.dto.response.PaymentResponse;
import com.motosphere.payment.dto.response.RazorpayOrderResponse;

public interface PaymentService {
	PaymentResponse makePayment(Long invoiceId, PaymentRequest request);

	RazorpayOrderResponse createRazorpayOrder(Long invoiceId);

	PaymentResponse verifyRazorpayPayment(Long invoiceId, RazorpayVerifyRequest request);

	// --- internal, service-to-service only (called by motosphere-backend) ---

	AdminDashboardResponse getAdminSummary();

	ManagerDashboardResponse getGarageSummary(Long garageId);
}
