package com.motosphere.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.LoginRequest;
import com.motosphere.dto.request.RegisterGarageManagerRequest;
import com.motosphere.dto.request.RegisterRequest;
import com.motosphere.dto.request.RegisterSuperAdminRequest;
import com.motosphere.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/register-super-admin")
	@Operation(description = "Creates the FIRST super admin account. Works exactly once - "
			+ "every call after a super admin already exists is rejected regardless of who calls it.")
	public ResponseEntity<?> registerSuperAdmin(@RequestBody @Valid RegisterSuperAdminRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerSuperAdmin(request));
	}

	@PostMapping("/register-garage-manager")
	@Operation(description = "Submits a garage + its manager account as a pending application. "
			+ "Neither is usable until a SUPER_ADMIN approves it via PATCH /garages/{garageId}/approve.")
	public ResponseEntity<?> registerGarageManager(@RequestBody @Valid RegisterGarageManagerRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerGarageManager(request));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}
