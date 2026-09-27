package com.motosphere.service;

import java.time.LocalDate;
import java.util.List;

import com.motosphere.dto.request.AppointmentRequest;
import com.motosphere.dto.request.AssignMechanicRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.AppointmentResponse;
import com.motosphere.dto.response.TimeSlotResponse;

public interface AppointmentService {
	ApiResponse bookAppointment(AppointmentRequest request);

	List<TimeSlotResponse> getAvailableSlots(Long garageId, LocalDate date);

	List<AppointmentResponse> getMyAppointments();

	List<AppointmentResponse> getGarageAppointments();

	List<AppointmentResponse> getMechanicAppointments();

	ApiResponse assignMechanic(Long appointmentId, AssignMechanicRequest request);

	ApiResponse cancelAppointment(Long appointmentId);
}
