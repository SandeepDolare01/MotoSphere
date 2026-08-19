package com.motosphere.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.VehicleRequest;
import com.motosphere.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
	private final VehicleService vehicleService;

	@PostMapping
	public ResponseEntity<?> addVehicle(@RequestBody @Valid VehicleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.addVehicle(request));
	}

	@GetMapping("/my")
	public ResponseEntity<?> getMyVehicles() {
		return ResponseEntity.ok(vehicleService.getMyVehicles());
	}
}
