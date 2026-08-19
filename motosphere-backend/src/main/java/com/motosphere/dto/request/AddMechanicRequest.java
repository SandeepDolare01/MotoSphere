package com.motosphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Used by a GARAGE_MANAGER to add a mechanic to their own garage.
 * Deliberately has no garageId/role field - the target garage is always the
 * calling manager's own (see UserServiceImpl#addMechanicToMyGarage), and the
 * role is always forced to MECHANIC. A GARAGE_MANAGER can never create
 * another manager or admin account this way - only a SUPER_ADMIN can, via
 * POST /users/staff.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class AddMechanicRequest {
	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})", message = "Password must be alphanumeric and strong")
	private String password;

	private String phoneNumber;
	private String specialization;
	private Integer experienceYears;
}
