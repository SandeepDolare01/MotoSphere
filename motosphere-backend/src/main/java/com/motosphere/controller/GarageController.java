package com.motosphere.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.motosphere.dto.request.ApproveGarageRequest;
import com.motosphere.dto.request.GarageRequest;
import com.motosphere.dto.request.RejectGarageRequest;
import com.motosphere.service.GarageService;
import com.motosphere.service.ImageFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/garages")
@RequiredArgsConstructor
public class GarageController {
	private final GarageService garageService;

	@PostMapping
	public ResponseEntity<?> createGarage(@RequestBody @Valid GarageRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(garageService.createGarage(request));
	}

	@GetMapping
	public ResponseEntity<?> getAllGarages() {
		return ResponseEntity.ok(garageService.getAllGarages());
	}

	@GetMapping("/pending")
	public ResponseEntity<?> getPendingGarages() {
		return ResponseEntity.ok(garageService.getPendingGarages());
	}

	@GetMapping("/{garageId}")
	public ResponseEntity<?> getGarageById(@PathVariable Long garageId) {
		return ResponseEntity.ok(garageService.getGarageById(garageId));
	}

	@PutMapping("/{garageId}")
	public ResponseEntity<?> updateGarage(@PathVariable Long garageId, @RequestBody @Valid GarageRequest request) {
		return ResponseEntity.ok(garageService.updateGarage(garageId, request));
	}

	@PatchMapping("/{garageId}/approve")
	public ResponseEntity<?> approveGarage(@PathVariable Long garageId,
			@RequestBody @Valid ApproveGarageRequest request) {
		return ResponseEntity.ok(garageService.approveGarage(garageId, request));
	}

	@PatchMapping("/{garageId}/reject")
	public ResponseEntity<?> rejectGarage(@PathVariable Long garageId, @RequestBody RejectGarageRequest request) {
		return ResponseEntity.ok(garageService.rejectGarage(garageId, request));
	}

	@DeleteMapping("/{garageId}")
	public ResponseEntity<?> deleteGarage(@PathVariable Long garageId) {
		return ResponseEntity.ok(garageService.deleteGarage(garageId));
	}

	// --- garage photo (single image per garage) ---

	@PostMapping(value = "/my/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadMyGarageImage(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(garageService.uploadMyGarageImage(file));
	}

	@DeleteMapping("/my/image")
	public ResponseEntity<?> deleteMyGarageImage() {
		return ResponseEntity.ok(garageService.deleteMyGarageImage());
	}

	@GetMapping("/{garageId}/image")
	public ResponseEntity<Resource> getGarageImageBytes(@PathVariable Long garageId) {
		ImageFile imageFile = garageService.loadGarageImage(garageId);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(imageFile.contentType()))
				.body(imageFile.resource());
	}
}
