package com.schedio.auth.security;

import java.util.Collection;
import java.util.List;

import com.schedio.user.UserAccount;
import com.schedio.user.UserRole;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record SchedioUserDetails(
	Long userId,
	Long businessId,
	String username,
	String password,
	UserRole role,
	boolean enabled
) implements UserDetails {

	public static SchedioUserDetails from(UserAccount user) {
		return new SchedioUserDetails(
			user.getId(),
			user.getBusinessId(),
			user.getEmail(),
			user.getPasswordHash(),
			user.getRole(),
			user.isActive()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
