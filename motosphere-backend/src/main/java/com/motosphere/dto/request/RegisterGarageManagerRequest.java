package com.motosphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Self-registration for a prospective garage + its manager, submitted
 * together as one application. Creates both records in PENDING state -
 * neither is usable (the garage isn't publicly listed, the manager account
 * can't log in) until a SUPER_ADMIN approves via
 * PATCH /garages/{garageId}/approve.
 *
 * Deliberately excludes commissionPercentage - that's a term MotoSphere
 * (the platform) sets when approving, not something the applicant proposes.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class RegisterGarageManagerRequest {
	// --- manager's own account ---
	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})", message = "Password must be alphanumeric and strong")
	private String password;

	private String phoneNumber;

	// --- garage being registered ---
	@NotBlank(message = "Garage name is required")
	private String garageName;

	private String ownerName;
	private String address;
	private String garageContactNumber;
	private String garageEmail;
}
