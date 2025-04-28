package ru.roms2002.messenger.server.config;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

	@Override
	public void commence(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
		httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	}
}
