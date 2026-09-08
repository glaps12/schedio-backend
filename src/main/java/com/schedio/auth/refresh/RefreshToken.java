package com.schedio.auth.refresh;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.schedio.user.UserAccount;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(name = "token_hash", nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(name = "family_id", nullable = false, length = 36)
	private String familyId;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	protected RefreshToken() {
	}

	public RefreshToken(UserAccount user, String tokenHash, String familyId, Instant expiresAt) {
		this.user = Objects.requireNonNull(user, "user is required");
		this.tokenHash = Objects.requireNonNull(tokenHash, "token hash is required");
		this.familyId = Objects.requireNonNull(familyId, "family id is required");
		this.expiresAt = Objects.requireNonNull(expiresAt, "expiration is required");
	}

	public void revoke(Instant revokedAt) {
		if (this.revokedAt == null) {
			this.revokedAt = Objects.requireNonNull(revokedAt, "revocation time is required");
		}
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public boolean isExpiredAt(Instant instant) {
		return !expiresAt.isAfter(instant);
	}

	public Long getId() {
		return id;
	}

	public UserAccount getUser() {
		return user;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public String getFamilyId() {
		return familyId;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
