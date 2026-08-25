package com.schedio.auth.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final HandlerExceptionResolver exceptionResolver;

	public RestAccessDeniedHandler(
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
	) {
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException exception
	) throws IOException, ServletException {
		exceptionResolver.resolveException(
			request,
			response,
			null,
			new ResponseStatusException(HttpStatus.FORBIDDEN, "Access is denied.")
		);
	}
}
