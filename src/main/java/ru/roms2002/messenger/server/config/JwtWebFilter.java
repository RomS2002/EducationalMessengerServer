package ru.roms2002.messenger.server.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.roms2002.messenger.server.service.UserService;

@Component
public class JwtWebFilter extends OncePerRequestFilter {

	@Autowired
	private UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		Cookie cookie = WebUtils.getCookie(request, "JWT");
		String jwtToken = null;

		if (cookie != null)
			jwtToken = cookie.getValue();
		else
			jwtToken = request.getHeader("JWT");

		System.out.println(jwtToken);

		if (jwtToken != null) {
			userService.authenticateUser(jwtToken);
			filterChain.doFilter(request, response);
			return;
		}
		filterChain.doFilter(request, response);
	}
}
