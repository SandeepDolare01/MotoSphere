package com.motosphere.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	boolean existsByInvoiceId(Long invoiceId);

	List<Payment> findAllByOrderByPaymentDateDesc();

	List<Payment> findByGarageIdOrderByPaymentDateDesc(Long garageId);
}
