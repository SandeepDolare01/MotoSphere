package com.motosphere.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.motosphere.payment.enums.PaymentMethod;
import com.motosphere.payment.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This service's OWN record of a completed payment - deliberately
 * denormalized (invoiceNumber, garageName, customerName, etc. copied in as
 * plain columns) rather than foreign-keyed to another service's tables,
 * because this database can't see motosphere-backend's tables at all. The
 * copy is taken once, at verification time, from InternalInvoiceDetailsResponse.
 *
 * This is the standard microservices trade-off: a bit of duplicated data,
 * in exchange for this service never needing a network call just to answer
 * "how much commission have we earned" - the dashboard queries run entirely
 * against this local table.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	// the invoiceId this payment is for, in motosphere-backend's database -
	// not a JPA relation (can't be, it's a different database), just a plain
	// reference id kept for traceability / the one-payment-per-invoice check
	@Column(nullable = false, unique = true)
	private Long invoiceId;

	@Column(nullable = false)
	private String invoiceNumber;

	@Column(nullable = false)
	private Long garageId;

	@Column(nullable = false)
	private String garageName;

	@Column(nullable = false)
	private String customerName;

	@Column(nullable = false)
	private String vehicleRegistrationNumber;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amountPaid;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal commissionAmount;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal garageAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	private String transactionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

	@Column(nullable = false)
	private LocalDateTime paymentDate;

	public Payment(Long invoiceId, String invoiceNumber, Long garageId, String garageName, String customerName,
			String vehicleRegistrationNumber, BigDecimal amountPaid, BigDecimal commissionAmount,
			BigDecimal garageAmount, PaymentMethod paymentMethod, String transactionId) {
		this.invoiceId = invoiceId;
		this.invoiceNumber = invoiceNumber;
		this.garageId = garageId;
		this.garageName = garageName;
		this.customerName = customerName;
		this.vehicleRegistrationNumber = vehicleRegistrationNumber;
		this.amountPaid = amountPaid;
		this.commissionAmount = commissionAmount;
		this.garageAmount = garageAmount;
		this.paymentMethod = paymentMethod;
		this.transactionId = transactionId;
		this.paymentStatus = PaymentStatus.SUCCESS;
	}

	@PrePersist
	protected void onCreate() {
		this.paymentDate = LocalDateTime.now();
	}
}
