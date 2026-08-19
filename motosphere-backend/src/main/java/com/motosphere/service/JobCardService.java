package com.motosphere.service;

import com.motosphere.dto.request.JobCardRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.JobCardResponse;

public interface JobCardService {
	ApiResponse createJobCard(Long appointmentId, JobCardRequest request);

	JobCardResponse getJobCard(Long jobCardId);

	// lets a customer/mechanic/manager go straight from an appointment to its
	// job card without already knowing the jobCardId - throws
	// ResourceNotFoundException (404) if no job card exists yet
	JobCardResponse getJobCardByAppointmentId(Long appointmentId);

	ApiResponse completeJobCard(Long jobCardId);
}
