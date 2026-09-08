package com.schedio.auth.config;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "schedio.auth")
public record AuthProperties(
	String issuer,
	String jwtSecret,
	Duration accessTokenTtl,
	Duration refreshTokenTtl
) {

	public AuthProperties {
		issuer = requireAbsoluteUri(issuer);
		jwtSecret = requireText(jwtSecret, "JWT secret");
		accessTokenTtl = requirePositive(accessTokenTtl, "access token TTL");
		refreshTokenTtl = requirePositive(refreshTokenTtl, "refresh token TTL");
	}

	private static String requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static String requireAbsoluteUri(String value) {
		var issuer = requireText(value, "issuer");
		try {
			if (!URI.create(issuer).isAbsolute()) {
				throw new IllegalArgumentException("issuer must be an absolute URI");
			}
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("issuer must be an absolute URI", exception);
		}
		return issuer;
	}

	private static Duration requirePositive(Duration value, String name) {
		Objects.requireNonNull(value, name + " is required");
		if (value.isZero() || value.isNegative()) {
			throw new IllegalArgumentException(name + " must be positive");
		}
		return value;
	}
}
