package com.motosphere.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.request.VehicleRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.VehicleResponse;
import com.motosphere.entity.User;
import com.motosphere.entity.Vehicle;
import com.motosphere.exception.DuplicateResourceException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.repository.UserRepository;
import com.motosphere.repository.VehicleRepository;
import com.motosphere.service.VehicleService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;

	@Override
	public ApiResponse addVehicle(VehicleRequest request) {
		if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber()))
			throw new DuplicateResourceException("A vehicle with this registration number is already registered");

		Long currentUserId = SecurityUtils.getCurrentUserId();
		User customer = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));

		Vehicle vehicle = new Vehicle();
		vehicle.setRegistrationNumber(request.getRegistrationNumber());
		vehicle.setManufacturer(request.getManufacturer());
		vehicle.setModel(request.getModel());
		vehicle.setManufacturingYear(request.getManufacturingYear());
		vehicle.setVehicleType(request.getVehicleType());
		vehicle.setFuelType(request.getFuelType());
		vehicle.setCustomer(customer);

		vehicleRepository.save(vehicle);
		return new ApiResponse("Vehicle registered!", "Success");
	}

	@Override
	public List<VehicleResponse> getMyVehicles() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return vehicleRepository.findByCustomer_UserId(currentUserId).stream()
				.map(v -> new VehicleResponse(v.getVehicleId(), v.getRegistrationNumber(), v.getManufacturer(),
						v.getModel(), v.getManufacturingYear(), v.getVehicleType(), v.getFuelType()))
				.toList();
	}

}
