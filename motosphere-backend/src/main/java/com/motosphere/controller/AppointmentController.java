package com.motosphere.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.AppointmentRequest;
import com.motosphere.dto.request.AssignMechanicRequest;
import com.motosphere.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
	private final AppointmentService appointmentService;

	@PostMapping
	public ResponseEntity<?> bookAppointment(@RequestBody @Valid AppointmentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request));
	}

	@GetMapping("/available-slots")
	public ResponseEntity<?> getAvailableSlots(@RequestParam Long garageId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(appointmentService.getAvailableSlots(garageId, date));
	}

	@GetMapping("/my")
	public ResponseEntity<?> getMyAppointments() {
		return ResponseEntity.ok(appointmentService.getMyAppointments());
	}

	@GetMapping("/garage")
	public ResponseEntity<?> getGarageAppointments() {
		return ResponseEntity.ok(appointmentService.getGarageAppointments());
	}

	@GetMapping("/mechanic")
	public ResponseEntity<?> getMechanicAppointments() {
		return ResponseEntity.ok(appointmentService.getMechanicAppointments());
	}

	@PatchMapping("/{appointmentId}/assign-mechanic")
	public ResponseEntity<?> assignMechanic(@PathVariable Long appointmentId,
			@RequestBody @Valid AssignMechanicRequest request) {
		return ResponseEntity.ok(appointmentService.assignMechanic(appointmentId, request));
	}

	@PatchMapping("/{appointmentId}/cancel")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
		return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
	}
}
