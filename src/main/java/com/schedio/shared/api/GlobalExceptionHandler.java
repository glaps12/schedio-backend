package com.schedio.shared.api;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		var validationErrors = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
			.toList();

		return createResponse(HttpStatus.BAD_REQUEST, "Validation failed.", request, validationErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiErrorResponse> handleConstraintViolation(
		ConstraintViolationException exception,
		HttpServletRequest request
	) {
		var validationErrors = exception.getConstraintViolations().stream()
			.map(violation -> new ValidationError(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			))
			.toList();

		return createResponse(HttpStatus.BAD_REQUEST, "Validation failed.", request, validationErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
		HttpMessageNotReadableException exception,
		HttpServletRequest request
	) {
		return createResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request.", request, List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNoResourceFound(
		NoResourceFoundException exception,
		HttpServletRequest request
	) {
		return createResponse(HttpStatus.NOT_FOUND, "Resource not found.", request, List.of());
	}

	@ExceptionHandler(ResponseStatusException.class)
	ResponseEntity<ApiErrorResponse> handleResponseStatus(
		ResponseStatusException exception,
		HttpServletRequest request
	) {
		var status = HttpStatus.valueOf(exception.getStatusCode().value());
		var message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();

		return createResponse(status, message, request, List.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpectedException(
		Exception exception,
		HttpServletRequest request
	) {
		LOGGER.error(
			"Unhandled {} for request {}",
			exception.getClass().getSimpleName(),
			request.getRequestURI()
		);

		return createResponse(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"An unexpected error occurred.",
			request,
			List.of()
		);
	}

	private ResponseEntity<ApiErrorResponse> createResponse(
		HttpStatus status,
		String message,
		HttpServletRequest request,
		List<ValidationError> validationErrors
	) {
		var response = new ApiErrorResponse(
			Instant.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			request.getRequestURI(),
			validationErrors
		);

		return ResponseEntity.status(status).body(response);
	}
}
