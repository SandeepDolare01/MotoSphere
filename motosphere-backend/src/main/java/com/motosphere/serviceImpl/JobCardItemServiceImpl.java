package com.motosphere.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.request.JobCardItemRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.entity.JobCard;
import com.motosphere.entity.JobCardItem;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.JobCardRepository;
import com.motosphere.service.JobCardItemService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JobCardItemServiceImpl implements JobCardItemService {
	private final JobCardRepository jobCardRepository;

	@Override
	public ApiResponse addItem(Long jobCardId, JobCardItemRequest request) {
		JobCard jobCard = jobCardRepository.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid jobCardId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		if (!jobCard.getMechanic().getUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This job card is not assigned to you");

		if (jobCard.getCompletionDate() != null)
			throw new BadRequestException("Items can't be added to a completed job card");

		// amount is ALWAYS computed server-side (quantity * unitPrice) - see JobCardItem's constructor
		JobCardItem item = new JobCardItem(request.getDescription(), request.getItemType(), request.getQuantity(),
				request.getUnitPrice());
		item.setJobCard(jobCard);
		jobCard.getItems().add(item);

		return new ApiResponse("Item added to job card!", "Success");
	}

}
