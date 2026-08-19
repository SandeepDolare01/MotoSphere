package com.motosphere.service;

import java.util.List;

import com.motosphere.dto.request.AppointmentRequest;
import com.motosphere.dto.request.AssignMechanicRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.AppointmentResponse;

public interface AppointmentService {
	ApiResponse bookAppointment(AppointmentRequest request);

	List<AppointmentResponse> getMyAppointments();

	List<AppointmentResponse> getGarageAppointments();

	List<AppointmentResponse> getMechanicAppointments();

	ApiResponse assignMechanic(Long appointmentId, AssignMechanicRequest request);

	ApiResponse cancelAppointment(Long appointmentId);
}
