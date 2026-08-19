package com.motosphere.dto.request;

import com.motosphere.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
	@NotNull(message = "paymentMethod is required")
	private PaymentMethod paymentMethod;

	// required for ONLINE, optional/ignored for CASH
	private String transactionId;
}
