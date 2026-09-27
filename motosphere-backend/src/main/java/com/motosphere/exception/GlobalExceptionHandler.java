package com.motosphere.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.motosphere.dto.response.ApiResponse;

/**
 * Centralized exception handling for the whole application. Every custom
 * exception maps to a specific HTTP status; anything unexpected falls
 * through to a generic 500 rather than leaking a stack trace to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), "Failed"));
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiResponse> handleBadRequest(BadRequestException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), "Failed"));
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiResponse> handleDuplicateResource(DuplicateResourceException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), "Failed"));
	}

	@ExceptionHandler(UnauthorizedActionException.class)
	public ResponseEntity<ApiResponse> handleUnauthorizedAction(UnauthorizedActionException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(e.getMessage(), "Failed"));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), "Failed"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleValidationErrors(MethodArgumentNotValidException e) {
		List<FieldError> fieldErrors = e.getFieldErrors();
		return fieldErrors.stream()
				.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleGenericException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), "Failed"));
	}

}
