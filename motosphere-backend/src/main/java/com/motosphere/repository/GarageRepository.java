package com.motosphere.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.motosphere.entity.Garage;
import com.motosphere.enums.ApprovalStatus;

public interface GarageRepository extends JpaRepository<Garage, Long> {
	List<Garage> findByApprovalStatus(ApprovalStatus approvalStatus);

	// row-level lock on the garage, held for the rest of the booking
	// transaction - forces a second concurrent booking for the same garage
	// to wait until the first one commits, so its capacity re-check sees
	// the first booking's write instead of racing it
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select g from Garage g where g.garageId = :id")
	Optional<Garage> findByIdForUpdate(@Param("id") Long id);
}
