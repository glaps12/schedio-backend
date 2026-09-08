package com.schedio.auth.refresh;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import com.schedio.auth.config.AuthProperties;
import com.schedio.shared.api.UnauthorizedException;
import com.schedio.user.UserAccount;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

	private static final int TOKEN_BYTE_LENGTH = 32;

	private final RefreshTokenRepository refreshTokenRepository;
	private final AuthProperties properties;
	private final Clock clock;
	private final SecureRandom secureRandom = new SecureRandom();

	public RefreshTokenService(
		RefreshTokenRepository refreshTokenRepository,
		AuthProperties properties,
		Clock clock
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.properties = properties;
		this.clock = clock;
	}

	@Transactional
	public IssuedRefreshToken issue(UserAccount user) {
		return createToken(user, UUID.randomUUID().toString());
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public RefreshTokenRotation rotate(String rawToken) {
		var now = clock.instant();
		var currentToken = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
			.orElseThrow(this::invalidRefreshToken);

		if (currentToken.isRevoked()) {
			revokeFamily(currentToken.getFamilyId(), now);
			throw invalidRefreshToken();
		}
		if (currentToken.isExpiredAt(now)) {
			currentToken.revoke(now);
			throw invalidRefreshToken();
		}
		if (!currentToken.getUser().isActive()) {
			revokeFamily(currentToken.getFamilyId(), now);
			throw invalidRefreshToken();
		}

		currentToken.revoke(now);
		var nextToken = createToken(currentToken.getUser(), currentToken.getFamilyId());
		return new RefreshTokenRotation(currentToken.getUser(), nextToken);
	}

	@Transactional
	public void revoke(String rawToken) {
		refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
			.ifPresent(token -> revokeFamily(token.getFamilyId(), clock.instant()));
	}

	private IssuedRefreshToken createToken(UserAccount user, String familyId) {
		var rawToken = generateToken();
		var expiresAt = clock.instant().plus(properties.refreshTokenTtl());
		refreshTokenRepository.save(new RefreshToken(user, hash(rawToken), familyId, expiresAt));
		return new IssuedRefreshToken(rawToken, expiresAt);
	}

	private void revokeFamily(String familyId, java.time.Instant revokedAt) {
		refreshTokenRepository.findByFamilyIdAndRevokedAtIsNull(familyId)
			.forEach(token -> token.revoke(revokedAt));
	}

	private String generateToken() {
		var bytes = new byte[TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String rawToken) {
		try {
			var digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}

	private UnauthorizedException invalidRefreshToken() {
		return new UnauthorizedException("Invalid refresh token.");
	}
}
