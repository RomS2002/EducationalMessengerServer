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
import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.InfoServerService;
import ru.roms2002.messenger.server.service.UserService;

@Component
@Order(99)
@Slf4j
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
			log.info("Окончание проверки на активность аккаунта");
			chain.doFilter(request, response);
			return;
		} else {
			log.info("Начало проверки на активность аккаунта");
			UserEntity user = userService.findByEmail(username);
			log.info("Вызов межсерверного запроса getUserDetailsByAdminpanelId");
			UserDetailsDTO userDetailsDTO = infoServerService
					.getUserDetailsByAdminpanelId(user.getAdminpanelId());
			log.info("Завершение межсерверного запроса");
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
			log.info("Окончание проверки на активность аккаунта");
			chain.doFilter(request, response);
		}
	}
}
