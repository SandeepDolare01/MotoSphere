package com.motosphere.payment.dto.internal;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Mirrors motosphere-backend's InternalInvoiceDetailsResponse field-for-field
// - this is what GET {core}/internal/invoices/{id} returns, deserialized
// here. paymentStatus is kept as a plain String (not an enum) since this
// service doesn't need to do anything with it beyond a simple equality
// check against "PAID", and it avoids needing an exactly-matching enum
// definition between two independently-deployed services.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetails {
	private Long invoiceId;
	private String invoiceNumber;
	private BigDecimal subtotal;
	private BigDecimal totalAmount;
	private String paymentStatus;

	private Long ownerCustomerUserId;
	private String customerName;
	private String customerEmail;

	private Long garageId;
	private String garageName;
	private Double garageCommissionPercentage;

	private String vehicleRegistrationNumber;
}
