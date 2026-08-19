package com.motosphere.payment.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
	private LocalDateTime timeStamp;
	private String message;
	private String status;

	public ApiResponse(String message, String status) {
		this.message = message;
		this.status = status;
		this.timeStamp = LocalDateTime.now();
	}
}
