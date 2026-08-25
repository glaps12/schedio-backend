package com.schedio.auth.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final HandlerExceptionResolver exceptionResolver;

	public RestAuthenticationEntryPoint(
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
	) {
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception
	) throws IOException, ServletException {
		exceptionResolver.resolveException(
			request,
			response,
			null,
			new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.")
		);
	}
}
