package com.schedio.auth.config;

import java.time.Clock;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthProperties.class)
public class AuthTokenConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	SecretKey jwtSecretKey(AuthProperties properties) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(properties.jwtSecret());
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalStateException("JWT_SECRET must be valid Base64.", exception);
		}

		if (keyBytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
		}

		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey secretKey) {
		return NimbusJwtEncoder.withSecretKey(secretKey)
			.algorithm(MacAlgorithm.HS256)
			.build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey secretKey, AuthProperties properties) {
		var decoder = NimbusJwtDecoder.withSecretKey(secretKey)
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
		return decoder;
	}
}
