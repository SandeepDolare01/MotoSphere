package com.motosphere.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.request.AppointmentRequest;
import com.motosphere.dto.request.AssignMechanicRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.AppointmentResponse;
import com.motosphere.entity.Appointment;
import com.motosphere.entity.Garage;
import com.motosphere.entity.User;
import com.motosphere.entity.Vehicle;
import com.motosphere.enums.AppointmentStatus;
import com.motosphere.enums.ApprovalStatus;
import com.motosphere.enums.Role;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.AppointmentRepository;
import com.motosphere.repository.GarageRepository;
import com.motosphere.repository.UserRepository;
import com.motosphere.repository.VehicleRepository;
import com.motosphere.service.AppointmentService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final VehicleRepository vehicleRepository;
	private final GarageRepository garageRepository;
	private final UserRepository userRepository;

	@Override
	public ApiResponse bookAppointment(AppointmentRequest request) {
		Long currentUserId = SecurityUtils.getCurrentUserId();

		Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid vehicleId!"));
		if (!vehicle.getCustomer().getUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This vehicle doesn't belong to you");

		Garage garage = garageRepository.findById(request.getGarageId())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));
		if (garage.getApprovalStatus() != ApprovalStatus.APPROVED)
			throw new BadRequestException("Invalid garageId!"); // don't leak that a pending/rejected garage exists

		boolean slotTaken = appointmentRepository.existsByVehicle_VehicleIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
				vehicle.getVehicleId(), request.getAppointmentDate(), request.getAppointmentTime(),
				AppointmentStatus.CANCELLED);
		if (slotTaken)
			throw new BadRequestException("This vehicle already has an appointment in that slot");

		Appointment appointment = new Appointment(request.getAppointmentDate(), request.getAppointmentTime(),
				request.getIssueDescription());
		appointment.setVehicle(vehicle);
		appointment.setGarage(garage);
		appointmentRepository.save(appointment);

		return new ApiResponse("Appointment booked!", "Success");
	}

	@Override
	public List<AppointmentResponse> getMyAppointments() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return appointmentRepository.getAppointmentsForCustomer(currentUserId);
	}

	@Override
	public List<AppointmentResponse> getGarageAppointments() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("This account isn't tied to a garage");
		return appointmentRepository.getAppointmentsForGarage(manager.getGarage().getGarageId());
	}

	@Override
	public List<AppointmentResponse> getMechanicAppointments() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return appointmentRepository.getAppointmentsForMechanic(currentUserId);
	}

	@Override
	public ApiResponse assignMechanic(Long appointmentId, AssignMechanicRequest request) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid appointmentId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
		if (manager.getGarage() == null || !manager.getGarage().getGarageId().equals(appointment.getGarage().getGarageId()))
			throw new UnauthorizedActionException("This appointment isn't at your garage");

		if (appointment.getStatus() != AppointmentStatus.BOOKED)
			throw new BadRequestException("Only a booked appointment can have a mechanic assigned");

		User mechanic = userRepository.findById(request.getMechanicId())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid mechanicId!"));
		if (mechanic.getRole() != Role.MECHANIC || mechanic.getGarage() == null
				|| !mechanic.getGarage().getGarageId().equals(manager.getGarage().getGarageId()))
			throw new BadRequestException("mechanicId must belong to a mechanic at your own garage");

		appointment.setMechanic(mechanic);
		appointment.setStatus(AppointmentStatus.ASSIGNED);

		return new ApiResponse("Mechanic assigned!", "Success");
	}

	@Override
	public ApiResponse cancelAppointment(Long appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid appointmentId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		if (!appointment.getVehicle().getCustomer().getUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This appointment doesn't belong to you");

		if (appointment.getStatus() != AppointmentStatus.BOOKED && appointment.getStatus() != AppointmentStatus.ASSIGNED)
			throw new BadRequestException("Only a booked or assigned appointment can be cancelled");

		appointment.setStatus(AppointmentStatus.CANCELLED);
		return new ApiResponse("Appointment cancelled!", "Success");
	}

}
