package com.motosphere.dto.response;

import java.math.BigDecimal;

import com.motosphere.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Served only from InternalController, to payment-service, never to a
// browser - carries the fields payment-service needs but doesn't own itself
// (it has no Invoice/Garage/Vehicle/User tables of its own), so it can
// validate ownership, size the Razorpay order correctly, and store a
// denormalized local copy for its own dashboard queries.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalInvoiceDetailsResponse {
	private Long invoiceId;
	private String invoiceNumber;
	private BigDecimal subtotal;
	private BigDecimal totalAmount;
	private PaymentStatus paymentStatus;

	private Long ownerCustomerUserId;
	private String customerName;
	private String customerEmail;

	private Long garageId;
	private String garageName;
	private Double garageCommissionPercentage;

	private String vehicleRegistrationNumber;
}
