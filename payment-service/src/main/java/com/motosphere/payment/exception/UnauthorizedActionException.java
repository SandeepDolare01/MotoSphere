package com.motosphere.payment.exception;

@SuppressWarnings("serial")
public class UnauthorizedActionException extends RuntimeException {
	public UnauthorizedActionException(String message) {
		super(message);
	}
}
