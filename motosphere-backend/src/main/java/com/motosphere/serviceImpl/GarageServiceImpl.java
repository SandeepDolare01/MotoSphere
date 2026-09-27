package com.motosphere.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.motosphere.dto.request.ApproveGarageRequest;
import com.motosphere.dto.request.GarageRequest;
import com.motosphere.dto.request.RejectGarageRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.GarageResponse;
import com.motosphere.dto.response.PendingGarageResponse;
import com.motosphere.entity.Garage;
import com.motosphere.entity.User;
import com.motosphere.enums.ApprovalStatus;
import com.motosphere.enums.Role;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.GarageRepository;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.FileStorageService;
import com.motosphere.service.GarageService;
import com.motosphere.service.ImageFile;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {
	private final GarageRepository garageRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;

	@Override
	public ApiResponse createGarage(GarageRequest request) {
		// direct SUPER_ADMIN creation - Garage's field default is APPROVED, so no
		// review step is needed here (the admin already vetted it by creating it)
		Garage garage = new Garage();
		applyRequest(garage, request);
		garageRepository.save(garage);
		return new ApiResponse("Garage created!", "Success");
	}

	@Override
	public List<GarageResponse> getAllGarages() {
		// public browsing only ever sees APPROVED garages
		return garageRepository.findByApprovalStatus(ApprovalStatus.APPROVED).stream().map(this::toDto).toList();
	}

	@Override
	public GarageResponse getGarageById(Long garageId) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		if (garage.getApprovalStatus() != ApprovalStatus.APPROVED)
			checkPendingOrRejectedAccess(garage);

		return toDto(garage);
	}

	@Override
	public ApiResponse updateGarage(Long garageId, GarageRequest request) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		// SUPER_ADMIN can update any garage; a GARAGE_MANAGER may only update their own
		if (!SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN)) {
			Long currentUserId = SecurityUtils.getCurrentUserId();
			User manager = userRepository.findById(currentUserId)
					.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
			boolean managesThisGarage = manager.getGarage() != null
					&& manager.getGarage().getGarageId().equals(garageId);
			if (!managesThisGarage)
				throw new UnauthorizedActionException("You can only update your own garage");
		}

		applyRequest(garage, request);
		return new ApiResponse("Garage updated!", "Success");
	}

	@Override
	public ApiResponse deleteGarage(Long garageId) {
		if (!garageRepository.existsById(garageId))
			throw new ResourceNotFoundException("Invalid garageId!");
		garageRepository.deleteById(garageId);
		return new ApiResponse("Garage deleted!", "Success");
	}

	@Override
	public List<PendingGarageResponse> getPendingGarages() {
		return garageRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream().map(garage -> {
			User applicant = findApplicantManager(garage);
			return new PendingGarageResponse(garage.getGarageId(), garage.getGarageName(), garage.getOwnerName(),
					garage.getAddress(), garage.getContactNumber(), garage.getEmail(),
					applicant == null ? null : applicant.getFirstName() + " " + applicant.getLastName(),
					applicant == null ? null : applicant.getEmail());
		}).toList();
	}

	@Override
	public ApiResponse approveGarage(Long garageId, ApproveGarageRequest request) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		if (garage.getApprovalStatus() != ApprovalStatus.PENDING)
			throw new BadRequestException("Only a pending garage application can be approved");

		garage.setApprovalStatus(ApprovalStatus.APPROVED);
		garage.setCommissionPercentage(request.getCommissionPercentage());

		// reactivate the applicant manager - they were created with active=false
		// and couldn't log in until this moment
		User applicant = findApplicantManager(garage);
		if (applicant != null)
			applicant.setActive(true);

		return new ApiResponse("Garage approved!", "Success");
	}

	@Override
	public ApiResponse rejectGarage(Long garageId, RejectGarageRequest request) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		if (garage.getApprovalStatus() != ApprovalStatus.PENDING)
			throw new BadRequestException("Only a pending garage application can be rejected");

		garage.setApprovalStatus(ApprovalStatus.REJECTED);
		// applicant manager stays inactive permanently - already false, left as-is

		return new ApiResponse("Garage application rejected", "Success");
	}

	// --- garage photo (single image per garage) ---

	@Override
	public ApiResponse uploadMyGarageImage(MultipartFile file) {
		Garage garage = myGarage();

		// only one photo is ever kept - delete the old file before storing the new one
		if (garage.getImageFileName() != null)
			fileStorageService.delete(garage.getImageFileName());

		String storedFileName = fileStorageService.store(file);
		garage.setImageFileName(storedFileName);
		garage.setImageContentType(file.getContentType());

		return new ApiResponse("Photo uploaded", "Success");
	}

	@Override
	public ApiResponse deleteMyGarageImage() {
		Garage garage = myGarage();

		if (garage.getImageFileName() == null)
			throw new BadRequestException("This garage has no photo to remove");

		fileStorageService.delete(garage.getImageFileName());
		garage.setImageFileName(null);
		garage.setImageContentType(null);

		return new ApiResponse("Photo removed", "Success");
	}

	@Override
	public ImageFile loadGarageImage(Long garageId) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		if (garage.getImageFileName() == null)
			throw new ResourceNotFoundException("This garage has no photo");

		return new ImageFile(fileStorageService.load(garage.getImageFileName()), garage.getImageContentType());
	}

	// --- helpers ---

	// Resolves the garage the currently-authenticated GARAGE_MANAGER belongs to.
	private Garage myGarage() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("You're not assigned to a garage yet");
		return manager.getGarage();
	}

	// a PENDING/REJECTED garage is only visible to a SUPER_ADMIN or the
	// applicant manager checking the status of their own application
	private void checkPendingOrRejectedAccess(Garage garage) {
		if (SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN))
			return;

		if (!SecurityUtils.isAuthenticated())
			throw new UnauthorizedActionException("This garage isn't public yet");

		Long currentUserId = SecurityUtils.getCurrentUserId();
		boolean isApplicant = garage.getUsers().stream()
				.anyMatch(u -> u.getRole() == Role.GARAGE_MANAGER && u.getUserId().equals(currentUserId));
		if (!isApplicant)
			throw new UnauthorizedActionException("This garage isn't public yet");
	}

	private User findApplicantManager(Garage garage) {
		return garage.getUsers().stream().filter(u -> u.getRole() == Role.GARAGE_MANAGER).findFirst().orElse(null);
	}

	private void applyRequest(Garage garage, GarageRequest request) {
		garage.setGarageName(request.getGarageName());
		garage.setOwnerName(request.getOwnerName());
		garage.setAddress(request.getAddress());
		garage.setContactNumber(request.getContactNumber());
		garage.setEmail(request.getEmail());
		garage.setOpeningTime(request.getOpeningTime());
		garage.setClosingTime(request.getClosingTime());
		garage.setCommissionPercentage(request.getCommissionPercentage());
	}

	private GarageResponse toDto(Garage garage) {
		String imageUrl = garage.getImageFileName() == null ? null : "/garages/" + garage.getGarageId() + "/image";
		return new GarageResponse(garage.getGarageId(), garage.getGarageName(), garage.getOwnerName(),
				garage.getAddress(), garage.getContactNumber(), garage.getEmail(), garage.getOpeningTime(),
				garage.getClosingTime(), garage.getRating(), garage.getCommissionPercentage(),
				garage.getApprovalStatus(), imageUrl);
	}
}
