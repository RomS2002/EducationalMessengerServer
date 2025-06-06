package ru.roms2002.messenger.server.config;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.ChatService;
import ru.roms2002.messenger.server.service.UserService;

@Component
public class SubscribtionInterceptor implements ChannelInterceptor {

	@Autowired
	private UserService userService;

	@Autowired
	private ChatService chatService;

	@Override
	@Transactional
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
		if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
			Principal principal = headerAccessor.getUser();
			UserEntity user = userService.findByEmail(principal.getName());

			if (("/topic/user/" + user.getId()).equals(headerAccessor.getDestination()))
				return message;

			List<ChatEntity> userChats = chatService.findByUserId(user.getId());
			for (ChatEntity chat : userChats)
				if (("/topic/chat/" + chat.getId()).equals(headerAccessor.getDestination()))
					return message;
			return null;
		}
		return message;
	}
}
