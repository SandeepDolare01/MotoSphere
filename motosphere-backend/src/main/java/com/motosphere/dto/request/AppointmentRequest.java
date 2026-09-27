package com.motosphere.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequest {
	@NotNull(message = "vehicleId is required")
	private Long vehicleId;

	@NotNull(message = "garageId is required")
	private Long garageId;

	@NotNull(message = "Appointment date is required")
	@FutureOrPresent(message = "Appointment date can't be in the past")
	private LocalDate appointmentDate;

	@NotNull(message = "Appointment time is required")
	private LocalTime appointmentTime;

	private String issueDescription;
}
