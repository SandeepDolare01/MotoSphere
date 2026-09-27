package com.motosphere.service;

import java.util.List;

import com.motosphere.dto.request.AddMechanicRequest;
import com.motosphere.dto.request.CreateStaffRequest;
import com.motosphere.dto.request.UpdateUserRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.UserResponse;

public interface UserService {
	ApiResponse createStaff(CreateStaffRequest request);

	List<UserResponse> getAllUsers();

	UserResponse getUserById(Long userId);

	ApiResponse updateOwnProfile(UpdateUserRequest request);

	ApiResponse deleteUser(Long userId);

	// --- garage-manager-scoped mechanic management ---

	// GARAGE_MANAGER only - always targets the caller's own garage
	ApiResponse addMechanicToMyGarage(AddMechanicRequest request);

	List<UserResponse> getMyGarageMechanics();

	// SUPER_ADMIN: any staff account. GARAGE_MANAGER: only a MECHANIC at their
	// own garage - never another manager or admin. See UserServiceImpl for the
	// exact scoping logic.
	ApiResponse deactivateStaff(Long userId);

	ApiResponse reactivateStaff(Long userId);
}
