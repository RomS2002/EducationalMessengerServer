package ru.roms2002.messenger.server.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpSession;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.utils.JwtUtil;

@Component
public class HandShakeInterceptor implements HandshakeInterceptor, WebSocketHandler {

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	private final Logger log = LoggerFactory.getLogger(HandShakeInterceptor.class);

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
		try {
			String jwtToken = request.getHeaders().getFirst("JWT");

			String username = jwtTokenUtil.getUserNameFromJwtToken(jwtToken);
			UserEntity user = userService.findByEmail(username);
			int userId = user.getId();

			if (request instanceof ServletServerHttpRequest servletRequest) {
				HttpSession session = servletRequest.getServletRequest().getSession();
				attributes.put("sessionId", session.getId());
				userService.getWsSessions().put(userId, session.getId());
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
			throws Exception {

	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception)
			throws Exception {

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
			throws Exception {
		log.info("connection closed : {}", session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
