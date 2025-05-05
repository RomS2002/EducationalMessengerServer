package ru.roms2002.messenger.server.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.UserService;

@Component
@Order(100)
public class UserOnlineFilter implements Filter {

	@Autowired
	private UserService userService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if (username.equals("anonymousUser")) {
			chain.doFilter(request, response);
			return;
		}

		UserEntity user = userService.findByEmail(username);
		userService.updateOnline(user);
		chain.doFilter(request, response);
	}

}
