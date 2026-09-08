package com.schedio.auth.token;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.schedio.auth.config.AuthProperties;
import com.schedio.user.UserAccount;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final AuthProperties properties;
	private final Clock clock;

	public JwtService(JwtEncoder jwtEncoder, AuthProperties properties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
		this.clock = clock;
	}

	public AccessToken createAccessToken(UserAccount user) {
		var issuedAt = clock.instant();
		var expiresAt = issuedAt.plus(properties.accessTokenTtl());
		var claims = JwtClaimsSet.builder()
			.issuer(properties.issuer())
			.subject(user.getId().toString())
			.id(UUID.randomUUID().toString())
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.claim("email", user.getEmail())
			.claim("roles", List.of(user.getRole().name()));

		if (user.getBusinessId() != null) {
			claims.claim("business_id", user.getBusinessId());
		}

		var headers = JwsHeader.with(MacAlgorithm.HS256)
			.type("JWT")
			.build();
		var token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims.build()));

		return new AccessToken(token.getTokenValue(), expiresAt);
	}
}
