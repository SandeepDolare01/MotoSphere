package com.motosphere.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.motosphere.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
	private Long paymentId;
	private LocalDateTime paymentDate;
	private String invoiceNumber;
	private String garageName;
	private String customerName;
	private String vehicleRegistrationNumber;
	private BigDecimal amountPaid;
	private BigDecimal commissionAmount;
	private BigDecimal garageAmount;
	private PaymentMethod paymentMethod;
	private String transactionId;
}
