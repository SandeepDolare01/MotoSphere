package com.motosphere.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.motosphere.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvoiceResponse {
	private Long invoiceId;
	private String invoiceNumber;
	private LocalDate invoiceDate;
	private BigDecimal subtotal;
	private Double gstPercentage;
	private BigDecimal gstAmount;
	private BigDecimal totalAmount;
	private PaymentStatus paymentStatus;
}
