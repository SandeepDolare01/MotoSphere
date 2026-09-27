package com.motosphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
	List<Vehicle> findByCustomer_UserId(Long customerId);

	boolean existsByRegistrationNumber(String registrationNumber);
}
