package com.motosphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Public self-registration - always creates a CUSTOMER account. GARAGE_MANAGER
 * and MECHANIC accounts are provisioned separately by a SUPER_ADMIN via
 * {@link CreateStaffRequest}, since they need to be tied to a specific garage.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class RegisterRequest {
	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})", message = "Password must be alphanumeric and strong")
	private String password;

	@NotBlank(message = "Phone number is required")
	private String phoneNumber;
}
