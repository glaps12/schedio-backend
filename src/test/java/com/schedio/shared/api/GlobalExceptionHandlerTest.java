package com.schedio.shared.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	void returnsValidationErrorsForInvalidRequestBody() throws Exception {
		mockMvc.perform(post("/test/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":""}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("Bad Request"))
			.andExpect(jsonPath("$.message").value("Validation failed."))
			.andExpect(jsonPath("$.path").value("/test/validation"))
			.andExpect(jsonPath("$.validationErrors[0].field").value("name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("name is required"));
	}

	@Test
	void returnsBadRequestForMalformedJson() throws Exception {
		mockMvc.perform(post("/test/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value("Malformed JSON request."))
			.andExpect(jsonPath("$.validationErrors").isEmpty());
	}

	@Test
	void preservesSafeNotFoundAndConflictMessages() throws Exception {
		mockMvc.perform(get("/test/not-found"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.error").value("Not Found"))
			.andExpect(jsonPath("$.message").value("Business not found."));

		mockMvc.perform(get("/test/conflict"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.error").value("Conflict"))
			.andExpect(jsonPath("$.message").value("Business state conflict."));
	}

	@Test
	void hidesUnexpectedExceptionDetails() throws Exception {
		mockMvc.perform(get("/test/error"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.error").value("Internal Server Error"))
			.andExpect(jsonPath("$.message").value("An unexpected error occurred."))
			.andExpect(jsonPath("$.path").value("/test/error"));
	}

	@RestController
	static class TestController {

		@PostMapping("/test/validation")
		void validate(@Valid @RequestBody ValidationRequest request) {
		}

		@GetMapping("/test/not-found")
		void notFound() {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found.");
		}

		@GetMapping("/test/conflict")
		void conflict() {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Business state conflict.");
		}

		@GetMapping("/test/error")
		void error() {
			throw new IllegalStateException("sensitive internal detail");
		}
	}

	record ValidationRequest(
		@NotBlank(message = "name is required") String name
	) {
	}
}
