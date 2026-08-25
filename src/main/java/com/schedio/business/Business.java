package com.schedio.business;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "businesses")
public class Business {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 100)
	private String timezone;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private Instant updatedAt;

	protected Business() {
	}

	public Business(String name, String timezone) {
		this.name = name;
		this.timezone = timezone;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTimezone() {
		return timezone;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
