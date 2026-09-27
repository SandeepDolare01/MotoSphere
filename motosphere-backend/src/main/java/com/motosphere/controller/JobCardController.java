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

import com.motosphere.dto.request.JobCardRequest;
import com.motosphere.service.JobCardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobcards")
@RequiredArgsConstructor
public class JobCardController {
	private final JobCardService jobCardService;

	@PostMapping("/appointment/{appointmentId}")
	public ResponseEntity<?> createJobCard(@PathVariable Long appointmentId, @RequestBody JobCardRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(jobCardService.createJobCard(appointmentId, request));
	}

	@GetMapping("/appointment/{appointmentId}")
	public ResponseEntity<?> getJobCardByAppointmentId(@PathVariable Long appointmentId) {
		return ResponseEntity.ok(jobCardService.getJobCardByAppointmentId(appointmentId));
	}

	@GetMapping("/{jobCardId}")
	public ResponseEntity<?> getJobCard(@PathVariable Long jobCardId) {
		return ResponseEntity.ok(jobCardService.getJobCard(jobCardId));
	}

	@PatchMapping("/{jobCardId}/complete")
	public ResponseEntity<?> completeJobCard(@PathVariable Long jobCardId) {
		return ResponseEntity.ok(jobCardService.completeJobCard(jobCardId));
	}
}
