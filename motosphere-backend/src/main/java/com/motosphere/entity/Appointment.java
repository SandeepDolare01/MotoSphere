package com.motosphere.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.motosphere.enums.AppointmentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * NOTE: deliberately no customerId field here, per spec - the customer is
 * always derived via Appointment -> Vehicle -> customer (User), never stored
 * redundantly on the appointment itself.
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "vehicle", "garage", "mechanic", "jobCard" })
public class Appointment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long appointmentId;

	@Column(nullable = false)
	private LocalDate appointmentDate;

	@Column(nullable = false)
	private LocalTime appointmentTime;

	@Column(length = 500)
	private String issueDescription;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AppointmentStatus status = AppointmentStatus.BOOKED;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// Many Appointments belong to one Vehicle
	@ManyToOne
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	// Many Appointments belong to one Garage
	@ManyToOne
	@JoinColumn(name = "garage_id", nullable = false)
	private Garage garage;

	// Many Appointments belong to one Mechanic (nullable - assigned later by the manager)
	@ManyToOne
	@JoinColumn(name = "mechanic_id")
	private User mechanic;

	// Appointment 1-----> 1 JobCard
	@OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
	private JobCard jobCard;

	public Appointment(LocalDate appointmentDate, LocalTime appointmentTime, String issueDescription) {
		super();
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.issueDescription = issueDescription;
		this.status = AppointmentStatus.BOOKED;
	}

}
