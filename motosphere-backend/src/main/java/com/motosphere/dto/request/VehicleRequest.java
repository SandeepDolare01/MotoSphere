package com.motosphere.dto.request;

import com.motosphere.enums.FuelType;
import com.motosphere.enums.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequest {
	@NotBlank(message = "Registration number is required")
	private String registrationNumber;

	@NotBlank(message = "Manufacturer is required")
	private String manufacturer;

	private String model;
	private Integer manufacturingYear;

	@NotNull(message = "Vehicle type is required")
	private VehicleType vehicleType;

	private FuelType fuelType;
}
