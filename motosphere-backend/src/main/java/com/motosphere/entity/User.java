package com.motosphere.entity;

import java.time.LocalDateTime;

import com.motosphere.enums.Role;

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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "password", "garage" })
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, length = 50)
	private String firstName;

	@Column(length = 50)
	private String lastName;

	@Column(nullable = false, unique = true, length = 60)
	private String email;

	@Column(nullable = false, length = 300)
	private String password;

	@Column(length = 14)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	// only populated for MECHANIC role
	@Column(length = 60)
	private String specialization;

	private Integer experienceYears;

	@Column(nullable = false)
	private boolean active = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// Many Users (managers/mechanics) belong to one Garage.
	// Customer and Super Admin always have garage = null.
	@ManyToOne
	@JoinColumn(name = "garage_id")
	private Garage garage;

}
