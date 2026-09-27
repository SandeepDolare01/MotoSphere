package com.motosphere.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.motosphere.dto.request.ApproveGarageRequest;
import com.motosphere.dto.request.GarageRequest;
import com.motosphere.dto.request.RejectGarageRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.GarageResponse;
import com.motosphere.dto.response.PendingGarageResponse;

public interface GarageService {
	// direct admin creation - starts APPROVED immediately, no review needed
	ApiResponse createGarage(GarageRequest request);

	// APPROVED garages only - what customers browse
	List<GarageResponse> getAllGarages();

	// APPROVED garages are visible to anyone; a PENDING/REJECTED garage is only
	// visible to a SUPER_ADMIN or the applicant manager checking their own status
	GarageResponse getGarageById(Long garageId);

	ApiResponse updateGarage(Long garageId, GarageRequest request);

	ApiResponse deleteGarage(Long garageId);

	List<PendingGarageResponse> getPendingGarages();

	ApiResponse approveGarage(Long garageId, ApproveGarageRequest request);

	ApiResponse rejectGarage(Long garageId, RejectGarageRequest request);

	// --- garage photo (single image per garage) ---

	// Uploads/replaces the currently-authenticated manager's own garage
	// photo. Only one is kept - a new upload deletes the old file.
	ApiResponse uploadMyGarageImage(MultipartFile file);

	ApiResponse deleteMyGarageImage();

	// Public - streams the actual image bytes. Throws ResourceNotFoundException
	// if the garage has no photo.
	ImageFile loadGarageImage(Long garageId);
}
