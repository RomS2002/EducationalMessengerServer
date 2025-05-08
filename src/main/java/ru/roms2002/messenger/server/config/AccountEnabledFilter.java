package ru.roms2002.messenger.server.config;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.InfoServerService;
import ru.roms2002.messenger.server.service.UserService;

@Component
@Order(99)
public class AccountEnabledFilter implements Filter {

	@Autowired
	private UserService userService;

	@Autowired
	private InfoServerService infoServerService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if (username.equals("anonymousUser")) {
			chain.doFilter(request, response);
			return;
		} else {

			UserEntity user = userService.findByEmail(username);
			UserDetailsDTO userDetailsDTO = infoServerService
					.getUserDetailsByAdminpanelId(user.getAdminpanelId());
			if (userDetailsDTO.getIsBlocked()) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(403);
				httpResponse.getOutputStream().println("Account is blocked");
				return;
			}
			if (userDetailsDTO.getEnabledUntil().before(new Date())) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(403);
				httpResponse.getOutputStream().println("Account is expired");
				return;
			}
			userService.updateOnline(user);
			chain.doFilter(request, response);
		}
	}
}
