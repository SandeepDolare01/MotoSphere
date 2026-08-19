package com.motosphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motosphere.entity.JobCardItem;

public interface JobCardItemRepository extends JpaRepository<JobCardItem, Long> {
	List<JobCardItem> findByJobCard_JobCardId(Long jobCardId);
}
