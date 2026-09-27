package com.motosphere.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayVerifyRequest {
	@NotBlank(message = "razorpayOrderId is required")
	private String razorpayOrderId;

	@NotBlank(message = "razorpayPaymentId is required")
	private String razorpayPaymentId;

	@NotBlank(message = "razorpaySignature is required")
	private String razorpaySignature;
}
