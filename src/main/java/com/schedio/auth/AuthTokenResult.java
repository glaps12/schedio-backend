package com.schedio.auth;

import com.schedio.auth.refresh.IssuedRefreshToken;
import com.schedio.auth.token.AccessToken;
import com.schedio.user.UserAccount;

public record AuthTokenResult(
	AccessToken accessToken,
	IssuedRefreshToken refreshToken,
	UserAccount user
) {
}
