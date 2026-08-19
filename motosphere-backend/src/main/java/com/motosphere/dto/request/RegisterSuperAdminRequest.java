package com.motosphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Used exactly once, by whoever stands up this deployment, to create the
 * very first SUPER_ADMIN account. The service layer enforces the "only once"
 * rule - this endpoint returns an error on every call after the first
 * SUPER_ADMIN already exists, regardless of who calls it or with what body.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class RegisterSuperAdminRequest {
	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})", message = "Password must be alphanumeric and strong")
	private String password;
}
