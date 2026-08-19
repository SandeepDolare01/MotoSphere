package com.motosphere.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GarageRequest {
	@NotBlank(message = "Garage name is required")
	private String garageName;

	private String ownerName;
	private String address;
	private String contactNumber;
	private String email;
	private LocalTime openingTime;
	private LocalTime closingTime;

	@NotNull(message = "Commission percentage is required")
	@DecimalMin(value = "0.0", message = "Commission percentage can't be negative")
	@DecimalMax(value = "100.0", message = "Commission percentage can't exceed 100")
	private Double commissionPercentage;
}
