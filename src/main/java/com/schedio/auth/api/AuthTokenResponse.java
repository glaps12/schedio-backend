package com.schedio.auth.api;

import java.time.Instant;

import com.schedio.auth.AuthTokenResult;

public record AuthTokenResponse(
	String tokenType,
	String accessToken,
	Instant accessTokenExpiresAt,
	String refreshToken,
	Instant refreshTokenExpiresAt,
	AuthenticatedUserResponse user
) {

	static AuthTokenResponse from(AuthTokenResult result) {
		return new AuthTokenResponse(
			"Bearer",
			result.accessToken().value(),
			result.accessToken().expiresAt(),
			result.refreshToken().value(),
			result.refreshToken().expiresAt(),
			AuthenticatedUserResponse.from(result.user())
		);
	}
}
