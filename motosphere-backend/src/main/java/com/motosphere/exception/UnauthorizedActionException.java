package com.motosphere.exception;

// raised when an authenticated user acts on a resource that isn't theirs
// (e.g. a mechanic touching another mechanic's job card, a manager acting
// outside their own garage)
public class UnauthorizedActionException extends RuntimeException {
	public UnauthorizedActionException(String message) {
		super(message);
	}
}
