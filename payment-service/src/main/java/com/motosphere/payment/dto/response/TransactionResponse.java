package com.motosphere.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.motosphere.payment.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Shape MUST match motosphere-backend's TransactionResponse field-for-field -
// motosphere-backend deserializes this JSON straight into its own copy of
// this class, so the two are two separate files with an implicit shared
// contract (a real system would formalize this with a shared OpenAPI spec).
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
