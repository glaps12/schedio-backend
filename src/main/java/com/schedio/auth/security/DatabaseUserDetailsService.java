package com.schedio.auth.security;

import com.schedio.user.UserAccount;
import com.schedio.user.UserAccountRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	public DatabaseUserDetailsService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return userAccountRepository.findByEmail(UserAccount.normalizeEmail(email))
			.map(SchedioUserDetails::from)
			.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}
}
