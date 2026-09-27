package com.motosphere.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motosphere.dto.response.AppointmentResponse;
import com.motosphere.entity.Appointment;
import com.motosphere.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	// customer's own appointments (derived via vehicle ownership, per spec - no customerId on Appointment)
	@Query("select new com.motosphere.dto.response.AppointmentResponse(a.appointmentId,a.appointmentDate,"
			+ "a.appointmentTime,a.issueDescription,a.status,v.registrationNumber,g.garageName,"
			+ "concat(m.firstName,' ',m.lastName)) "
			+ "from Appointment a join a.vehicle v join a.garage g left join a.mechanic m "
			+ "where v.customer.userId = :customerId order by a.appointmentDate desc")
	List<AppointmentResponse> getAppointmentsForCustomer(@Param("customerId") Long customerId);

	// full queue for a garage manager's own garage
	@Query("select new com.motosphere.dto.response.AppointmentResponse(a.appointmentId,a.appointmentDate,"
			+ "a.appointmentTime,a.issueDescription,a.status,v.registrationNumber,g.garageName,"
			+ "concat(m.firstName,' ',m.lastName)) "
			+ "from Appointment a join a.vehicle v join a.garage g left join a.mechanic m "
			+ "where g.garageId = :garageId order by a.appointmentDate desc")
	List<AppointmentResponse> getAppointmentsForGarage(@Param("garageId") Long garageId);

	// a mechanic's own assigned appointments
	@Query("select new com.motosphere.dto.response.AppointmentResponse(a.appointmentId,a.appointmentDate,"
			+ "a.appointmentTime,a.issueDescription,a.status,v.registrationNumber,g.garageName,"
			+ "concat(m.firstName,' ',m.lastName)) "
			+ "from Appointment a join a.vehicle v join a.garage g left join a.mechanic m "
			+ "where m.userId = :mechanicId order by a.appointmentDate desc")
	List<AppointmentResponse> getAppointmentsForMechanic(@Param("mechanicId") Long mechanicId);

	boolean existsByVehicle_VehicleIdAndAppointmentDateAndAppointmentTimeAndStatusNot(Long vehicleId,
			LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

	// how many active (non-cancelled) appointments already occupy this exact
	// garage/date/time slot - compared against the garage's active mechanic
	// count to decide whether the slot still has room. Used both to build the
	// available-slots list and to re-validate at actual booking time (a
	// second customer could grab the last spot between the two calls).
	long countByGarage_GarageIdAndAppointmentDateAndAppointmentTimeAndStatusNot(Long garageId,
			LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);
}
