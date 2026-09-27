package com.motosphere.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.motosphere.enums.AppointmentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AppointmentResponse {
	private Long appointmentId;
	private LocalDate appointmentDate;
	private LocalTime appointmentTime;
	private String issueDescription;
	private AppointmentStatus status;
	private String vehicleRegistrationNumber;
	private String garageName;
	private String mechanicName; // null until assigned
}
