package com.motosphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.JobCard;

public interface JobCardRepository extends JpaRepository<JobCard, Long> {
	Optional<JobCard> findByAppointment_AppointmentId(Long appointmentId);

	boolean existsByAppointment_AppointmentId(Long appointmentId);
}
