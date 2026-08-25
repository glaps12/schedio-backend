package com.schedio.auth.security;

import com.schedio.shared.api.GlobalExceptionHandler;
import testsupport.security.SecurityTestController;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
	SecurityConfig.class,
	RestAuthenticationEntryPoint.class,
	RestAccessDeniedHandler.class,
	GlobalExceptionHandler.class,
	SecurityTestController.class
})
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void allowsOperationalAndDocumentationEndpointsWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk());

		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk());

		mockMvc.perform(get("/swagger-ui.html"))
			.andExpect(status().isOk());
	}

	@Test
	void returnsStandardUnauthorizedResponseForProtectedEndpoint() throws Exception {
		mockMvc.perform(get("/test/security/authenticated"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.error").value("Unauthorized"))
			.andExpect(jsonPath("$.message").value("Authentication is required."))
			.andExpect(jsonPath("$.path").value("/test/security/authenticated"))
			.andExpect(jsonPath("$.validationErrors").isEmpty());
	}

	@Test
	void allowsAuthenticatedRequestsWithoutCreatingASession() throws Exception {
		mockMvc.perform(get("/test/security/authenticated").with(user("test-user")))
			.andExpect(status().isOk())
			.andExpect(content().string("authenticated"))
			.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@Test
	void returnsStandardForbiddenResponseWhenRoleIsInsufficient() throws Exception {
		mockMvc.perform(get("/test/security/owner").with(user("employee").roles("EMPLOYEE")))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.error").value("Forbidden"))
			.andExpect(jsonPath("$.message").value("Access is denied."))
			.andExpect(jsonPath("$.path").value("/test/security/owner"));
	}

	@Test
	void allowsMethodInvocationForRequiredRole() throws Exception {
		mockMvc.perform(get("/test/security/owner").with(user("owner").roles("BUSINESS_OWNER")))
			.andExpect(status().isOk())
			.andExpect(content().string("owner"));
	}

	@Test
	void acceptsStatelessPostWithoutCsrfToken() throws Exception {
		mockMvc.perform(post("/test/security/authenticated").with(user("test-user")))
			.andExpect(status().isOk())
			.andExpect(content().string("authenticated"));
	}

	@Test
	void usesBcryptForPasswordHashing() {
		var rawPassword = "example-password";
		var passwordHash = passwordEncoder.encode(rawPassword);

		assertThat(passwordHash).isNotEqualTo(rawPassword);
		assertThat(passwordHash).startsWith("$2");
		assertThat(passwordEncoder.matches(rawPassword, passwordHash)).isTrue();
	}
}
