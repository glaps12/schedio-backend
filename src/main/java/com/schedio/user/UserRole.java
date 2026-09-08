package com.schedio.user;

public enum UserRole {

	PLATFORM_ADMIN(false),
	BUSINESS_OWNER(true),
	EMPLOYEE(true),
	CUSTOMER(true);

	private final boolean businessRequired;

	UserRole(boolean businessRequired) {
		this.businessRequired = businessRequired;
	}

	public boolean isBusinessRequired() {
		return businessRequired;
	}
}
