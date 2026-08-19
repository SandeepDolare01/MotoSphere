package com.motosphere.dto.response;

import com.motosphere.enums.FuelType;
import com.motosphere.enums.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VehicleResponse {
	private Long vehicleId;
	private String registrationNumber;
	private String manufacturer;
	private String model;
	private Integer manufacturingYear;
	private VehicleType vehicleType;
	private FuelType fuelType;
}
