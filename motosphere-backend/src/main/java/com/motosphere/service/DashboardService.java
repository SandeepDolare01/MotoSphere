package com.motosphere.service;

import com.motosphere.dto.response.AdminDashboardResponse;
import com.motosphere.dto.response.ManagerDashboardResponse;

public interface DashboardService {
	AdminDashboardResponse getAdminDashboard();

	// Scoped to whichever garage the currently-authenticated manager belongs to.
	ManagerDashboardResponse getManagerDashboard();
}
