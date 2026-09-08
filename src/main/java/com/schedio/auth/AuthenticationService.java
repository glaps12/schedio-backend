package com.schedio.auth;

import com.schedio.auth.refresh.RefreshTokenService;
import com.schedio.auth.refresh.IssuedRefreshToken;
import com.schedio.auth.security.SchedioUserDetails;
import com.schedio.auth.token.JwtService;
import com.schedio.shared.api.UnauthorizedException;
import com.schedio.user.UserAccountRepository;
import com.schedio.user.UserAccount;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final UserAccountRepository userAccountRepository;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthenticationService(
		AuthenticationManager authenticationManager,
		UserAccountRepository userAccountRepository,
		JwtService jwtService,
		RefreshTokenService refreshTokenService
	) {
		this.authenticationManager = authenticationManager;
		this.userAccountRepository = userAccountRepository;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public AuthTokenResult login(String email, String password) {
		SchedioUserDetails principal;
		try {
			var authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email, password)
			);
			principal = (SchedioUserDetails) authentication.getPrincipal();
		}
		catch (AuthenticationException exception) {
			throw new UnauthorizedException("Invalid email or password.");
		}

		var user = userAccountRepository.findById(principal.userId())
			.filter(account -> account.isActive())
			.orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

		return createTokenResult(user, refreshTokenService.issue(user));
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public AuthTokenResult refresh(String rawRefreshToken) {
		var rotation = refreshTokenService.rotate(rawRefreshToken);
		return createTokenResult(rotation.user(), rotation.refreshToken());
	}

	public void logout(String rawRefreshToken) {
		refreshTokenService.revoke(rawRefreshToken);
	}

	private AuthTokenResult createTokenResult(
		UserAccount user,
		IssuedRefreshToken refreshToken
	) {
		return new AuthTokenResult(jwtService.createAccessToken(user), refreshToken, user);
	}
}
