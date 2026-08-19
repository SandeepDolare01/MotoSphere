package com.motosphere.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.CreateStaffRequest;
import com.motosphere.dto.request.UpdateUserRequest;
import com.motosphere.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/staff")
	public ResponseEntity<?> createStaff(@RequestBody @Valid CreateStaffRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStaff(request));
	}

	@GetMapping
	public ResponseEntity<?> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@PutMapping("/me")
	public ResponseEntity<?> updateOwnProfile(@RequestBody UpdateUserRequest request) {
		return ResponseEntity.ok(userService.updateOwnProfile(request));
	}

	@PatchMapping("/{userId}/deactivate")
	public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.deactivateStaff(userId));
	}

	@PatchMapping("/{userId}/reactivate")
	public ResponseEntity<?> reactivateUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.reactivateStaff(userId));
	}

	@DeleteMapping("/{userId}")
	@Operation(description = "Hard delete. Prefer /deactivate for staff with any job card/appointment "
			+ "history - deleting a mechanic referenced by an existing job card will fail on the "
			+ "database's foreign key constraint.")
	public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.deleteUser(userId));
	}
}
