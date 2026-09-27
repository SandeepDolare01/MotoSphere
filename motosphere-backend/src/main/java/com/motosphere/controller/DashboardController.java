package com.motosphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	private final DashboardService dashboardService;

	@GetMapping("/admin")
	public ResponseEntity<?> getAdminDashboard() {
		return ResponseEntity.ok(dashboardService.getAdminDashboard());
	}

	@GetMapping("/manager")
	public ResponseEntity<?> getManagerDashboard() {
		return ResponseEntity.ok(dashboardService.getManagerDashboard());
	}
}
