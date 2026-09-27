package com.motosphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	Optional<Invoice> findByJobCard_JobCardId(Long jobCardId);

	boolean existsByJobCard_JobCardId(Long jobCardId);
}
