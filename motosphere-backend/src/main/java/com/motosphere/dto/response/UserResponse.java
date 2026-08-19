package com.motosphere.dto.response;

import com.motosphere.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
	private Long userId;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private Role role;
	private String specialization;
	private Integer experienceYears;
	private boolean active;
	private Long garageId; // null for CUSTOMER / SUPER_ADMIN
	private String garageName;
}
