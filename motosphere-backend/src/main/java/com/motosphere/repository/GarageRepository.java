package com.motosphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.Garage;
import com.motosphere.enums.ApprovalStatus;

public interface GarageRepository extends JpaRepository<Garage, Long> {
	List<Garage> findByApprovalStatus(ApprovalStatus approvalStatus);
}
