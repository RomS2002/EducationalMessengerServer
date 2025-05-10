package ru.roms2002.messenger.server.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.service.UserService;

@Component
@Slf4j
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

		if (jwtToken != null) {
			userService.authenticateUser(jwtToken);
			log.info("Выполнен вход под аккаунтом {}",
					SecurityContextHolder.getContext().getAuthentication().getName());
			filterChain.doFilter(request, response);
			return;
		}
		log.debug("Выполнен вход под анонимным акканутом");
		filterChain.doFilter(request, response);
	}
}
