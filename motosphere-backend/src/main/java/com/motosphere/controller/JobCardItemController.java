package com.motosphere.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.dto.request.JobCardItemRequest;
import com.motosphere.service.JobCardItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobcards")
@RequiredArgsConstructor
public class JobCardItemController {
	private final JobCardItemService jobCardItemService;

	@PostMapping("/{jobCardId}/items")
	public ResponseEntity<?> addItem(@PathVariable Long jobCardId, @RequestBody @Valid JobCardItemRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(jobCardItemService.addItem(jobCardId, request));
	}
}
