package com.motosphere.payment.dto.request;

import com.motosphere.payment.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
	@NotNull(message = "paymentMethod is required")
	private PaymentMethod paymentMethod;

	private String transactionId;
}
