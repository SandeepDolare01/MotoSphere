package com.motosphere.dto.request;

import lombok.Getter;
import lombok.Setter;

/** Self-service profile update - deliberately excludes role/garage/email/active. */
@Getter
@Setter
public class UpdateUserRequest {
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private String specialization;
	private Integer experienceYears;
}
