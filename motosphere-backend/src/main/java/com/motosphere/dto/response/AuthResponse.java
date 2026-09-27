package com.motosphere.dto.response;

import com.motosphere.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
	private String message;
	private String token;
	private Long userId;
	private Role role;
}
