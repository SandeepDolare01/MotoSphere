package com.motosphere.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.User;
import com.motosphere.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findByGarage_GarageId(Long garageId);

	List<User> findByGarage_GarageIdAndRole(Long garageId, Role role);

	boolean existsByRole(Role role);
}
