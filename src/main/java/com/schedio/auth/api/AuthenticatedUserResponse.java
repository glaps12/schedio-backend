package com.schedio.auth.api;

import com.schedio.user.UserAccount;
import com.schedio.user.UserRole;

public record AuthenticatedUserResponse(
	Long id,
	Long businessId,
	String email,
	UserRole role
) {

	static AuthenticatedUserResponse from(UserAccount user) {
		return new AuthenticatedUserResponse(
			user.getId(),
			user.getBusinessId(),
			user.getEmail(),
			user.getRole()
		);
	}
}
