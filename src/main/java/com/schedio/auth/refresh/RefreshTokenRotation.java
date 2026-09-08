package com.schedio.auth.refresh;

import com.schedio.user.UserAccount;

public record RefreshTokenRotation(
	UserAccount user,
	IssuedRefreshToken refreshToken
) {
}
