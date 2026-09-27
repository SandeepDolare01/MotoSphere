package com.motosphere.dto.request;

import com.motosphere.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Used by a SUPER_ADMIN to provision a GARAGE_MANAGER or MECHANIC account
 * tied to a specific garage.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class CreateStaffRequest {
	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Password is required")
	private String password;

	private String phoneNumber;

	@NotNull(message = "Role is required")
	private Role role; // must be GARAGE_MANAGER or MECHANIC

	@NotNull(message = "garageId is required")
	private Long garageId;

	private String specialization; // only meaningful for MECHANIC
	private Integer experienceYears; // only meaningful for MECHANIC
}
