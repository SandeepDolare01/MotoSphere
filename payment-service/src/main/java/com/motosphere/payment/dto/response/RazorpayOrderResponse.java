package com.motosphere.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RazorpayOrderResponse {
	private String razorpayOrderId;
	private String razorpayKeyId;
	private long amount; // in paise
	private String currency;
	private Long invoiceId;
}
