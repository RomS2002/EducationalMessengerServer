package ru.roms2002.messenger.server.config;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import ru.roms2002.messenger.server.service.UserService;

@Configuration
@EnableWebSocketMessageBroker
public class WsConfig implements WebSocketMessageBrokerConfigurer {

	@Autowired
	private UserService userService;

	@Autowired
	private SubscribtionInterceptor subscribtionInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/messenger");
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
			@Override
			public WebSocketHandler decorate(final WebSocketHandler handler) {
				return new WebSocketHandlerDecorator(handler) {
					@Override
					public void afterConnectionEstablished(final WebSocketSession session)
							throws Exception {
						String username = session.getPrincipal().getName();
						if (!userService.getWsSessions().containsKey(username)) {
							CopyOnWriteArrayList<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
							sessionList.add(session);
							userService.getWsSessions().put(username, sessionList);
						} else {
							userService.getWsSessions().get(username).add(session);
						}

						super.afterConnectionEstablished(session);
					}

					@Override
					public void afterConnectionClosed(WebSocketSession session,
							CloseStatus closeStatus) throws Exception {
						String username = session.getPrincipal().getName();
						userService.getWsSessions().get(username).remove(session);
						if (userService.getWsSessions().get(username).isEmpty()) {
							userService.getWsSessions().remove(username);
						}

						super.afterConnectionClosed(session, closeStatus);
					}
				};
			}
		});
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(subscribtionInterceptor);
	}
}
