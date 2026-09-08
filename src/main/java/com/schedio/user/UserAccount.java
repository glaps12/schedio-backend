package com.schedio.user;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id")
	private Long businessId;

	@Column(nullable = false, length = 254, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRole role;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private Instant updatedAt;

	protected UserAccount() {
	}

	public UserAccount(Long businessId, String email, String passwordHash, UserRole role) {
		this.role = Objects.requireNonNull(role, "role is required");
		validateBusinessScope(businessId, role);
		this.businessId = businessId;
		this.email = normalizeEmail(email);
		this.passwordHash = Objects.requireNonNull(passwordHash, "password hash is required");
	}

	public static String normalizeEmail(String email) {
		return Objects.requireNonNull(email, "email is required")
			.strip()
			.toLowerCase(Locale.ROOT);
	}

	private static void validateBusinessScope(Long businessId, UserRole role) {
		if (role.isBusinessRequired() && businessId == null) {
			throw new IllegalArgumentException("business id is required for tenant roles");
		}
		if (!role.isBusinessRequired() && businessId != null) {
			throw new IllegalArgumentException("platform administrators cannot belong to a business");
		}
	}

	public Long getId() {
		return id;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
