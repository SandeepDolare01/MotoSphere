package com.motosphere.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.motosphere.payment.enums.PaymentMethod;
import com.motosphere.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {
	private Long paymentId;
	private BigDecimal amountPaid;
	private BigDecimal commissionAmount;
	private BigDecimal garageAmount;
	private PaymentMethod paymentMethod;
	private String transactionId;
	private PaymentStatus paymentStatus;
	private LocalDateTime paymentDate;
}
