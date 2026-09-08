package com.schedio.auth.api;

import jakarta.validation.Valid;

import com.schedio.auth.AuthenticationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/login")
	AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return AuthTokenResponse.from(
			authenticationService.login(request.email(), request.password())
		);
	}

	@PostMapping("/refresh")
	AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return AuthTokenResponse.from(authenticationService.refresh(request.refreshToken()));
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		authenticationService.logout(request.refreshToken());
		return ResponseEntity.noContent().build();
	}
}
