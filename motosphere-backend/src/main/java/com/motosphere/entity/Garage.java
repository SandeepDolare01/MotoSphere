package com.motosphere.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.motosphere.enums.ApprovalStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "garages")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "users", "appointments" })
public class Garage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long garageId;

	@Column(nullable = false, length = 100)
	private String garageName;

	@Column(length = 100)
	private String ownerName;

	@Column(length = 300)
	private String address;

	@Column(length = 14)
	private String contactNumber;

	@Column(length = 60)
	private String email;

	private LocalTime openingTime;

	private LocalTime closingTime;

	private Double rating;

	// e.g. 10.0 means MotoSphere takes a 10% commission on every job card
	// subtotal. Nullable because a self-registered garage doesn't have this set
	// until a SUPER_ADMIN approves it and decides the terms.
	private Double commissionPercentage;

	// A single photo per garage - the actual file lives on disk (see
	// FileStorageService), this is just the generated filename it's stored
	// under. Null means no photo has been uploaded yet, in which case the
	// browse-garages page falls back to a placeholder image.
	private String imageFileName;

	private String imageContentType;

	// A garage created directly by a SUPER_ADMIN (POST /garages) starts
	// APPROVED. A garage created via self-registration (POST
	// /auth/register-garage-manager) starts PENDING and is invisible to public
	// browsing / unusable until a SUPER_ADMIN approves it.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// Garage 1-----> * User (managers + mechanics)
	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL)
	private List<User> users = new ArrayList<>();

	// Garage 1-----> * Appointment
	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL)
	private List<Appointment> appointments = new ArrayList<>();

	public Garage(String garageName, String ownerName, String address, String contactNumber, String email) {
		super();
		this.garageName = garageName;
		this.ownerName = ownerName;
		this.address = address;
		this.contactNumber = contactNumber;
		this.email = email;
		this.approvalStatus = ApprovalStatus.PENDING;
	}

}
