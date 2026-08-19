package com.motosphere.entity;

import java.time.LocalDateTime;

import com.motosphere.enums.FuelType;
import com.motosphere.enums.VehicleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
public class Vehicle {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long vehicleId;

	@Column(nullable = false, unique = true, length = 20)
	private String registrationNumber;

	@Column(nullable = false, length = 50)
	private String manufacturer;

	@Column(length = 50)
	private String model;

	private Integer manufacturingYear;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VehicleType vehicleType;

	@Enumerated(EnumType.STRING)
	private FuelType fuelType;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// Many Vehicles belong to one Customer (User)
	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

}
