package com.motosphere.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.motosphere.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "jobCard" })
public class Invoice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long invoiceId;

	@Column(nullable = false, unique = true, length = 40)
	private String invoiceNumber;

	@Column(nullable = false)
	private LocalDate invoiceDate;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal subtotal;

	// fixed at 18% per business rule
	@Column(nullable = false)
	private Double gstPercentage = 18.0;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal gstAmount;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	// This is the sole source of truth for whether an invoice is paid.
	// The actual Payment record (amount split, transaction id, method) now
	// lives entirely in payment-service's own database - this service never
	// stores payment details itself, only this status flag, which
	// payment-service flips via PATCH /internal/invoices/{id}/mark-paid
	// once it has independently verified the payment succeeded.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;

	// Invoice 1-----> 1 JobCard
	@OneToOne
	@JoinColumn(name = "job_card_id", nullable = false, unique = true)
	private JobCard jobCard;

	public Invoice(String invoiceNumber, LocalDate invoiceDate, BigDecimal subtotal, BigDecimal gstAmount,
			BigDecimal totalAmount) {
		super();
		this.invoiceNumber = invoiceNumber;
		this.invoiceDate = invoiceDate;
		this.subtotal = subtotal;
		this.gstPercentage = 18.0;
		this.gstAmount = gstAmount;
		this.totalAmount = totalAmount;
		this.paymentStatus = PaymentStatus.PENDING;
	}

}
