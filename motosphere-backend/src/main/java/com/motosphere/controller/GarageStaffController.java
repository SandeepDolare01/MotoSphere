package com.motosphere.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.AddMechanicRequest;
import com.motosphere.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * A GARAGE_MANAGER's self-service mechanic management for their own garage -
 * separate from the SUPER_ADMIN-only, any-garage staff endpoints on
 * UserController (POST /users/staff, PATCH /users/{id}/deactivate, etc).
 * Every method here is scoped to the caller's own garage in the service
 * layer (UserServiceImpl#addMechanicToMyGarage etc) - a manager can never
 * reach another garage's mechanics through these endpoints.
 */
@RestController
@RequestMapping("/garages/my/mechanics")
@RequiredArgsConstructor
public class GarageStaffController {
	private final UserService userService;

	@PostMapping
	public ResponseEntity<?> addMechanic(@RequestBody @Valid AddMechanicRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.addMechanicToMyGarage(request));
	}

	@GetMapping
	public ResponseEntity<?> getMyMechanics() {
		return ResponseEntity.ok(userService.getMyGarageMechanics());
	}

	@PatchMapping("/{mechanicUserId}/deactivate")
	@Operation(description = "Removes a mechanic's access without deleting their record - "
			+ "existing job cards/appointments referencing them are preserved.")
	public ResponseEntity<?> deactivateMechanic(@PathVariable Long mechanicUserId) {
		return ResponseEntity.ok(userService.deactivateStaff(mechanicUserId));
	}

	@PatchMapping("/{mechanicUserId}/reactivate")
	public ResponseEntity<?> reactivateMechanic(@PathVariable Long mechanicUserId) {
		return ResponseEntity.ok(userService.reactivateStaff(mechanicUserId));
	}
}
