package com.motosphere.service;

import java.util.List;

import com.motosphere.dto.request.VehicleRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.VehicleResponse;

public interface VehicleService {
	ApiResponse addVehicle(VehicleRequest request);

	List<VehicleResponse> getMyVehicles();
}
