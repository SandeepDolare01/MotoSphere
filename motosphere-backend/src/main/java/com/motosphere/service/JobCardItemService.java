package com.motosphere.service;

import com.motosphere.dto.request.JobCardItemRequest;
import com.motosphere.dto.response.ApiResponse;

public interface JobCardItemService {
	ApiResponse addItem(Long jobCardId, JobCardItemRequest request);
}
