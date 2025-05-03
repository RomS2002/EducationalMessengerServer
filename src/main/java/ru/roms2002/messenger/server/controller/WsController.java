package ru.roms2002.messenger.server.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.roms2002.messenger.server.dto.MessageSendDTO;
import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.WebSocketService;

@RestController
public class WsController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private WebSocketService webSocketService;

	@MessageMapping("/chat/{chatId}")
	public WebSocketDTO operationWithChat(@Payload WebSocketDTO payload, Principal user,
			@DestinationVariable Integer chatId) {
		String operation = payload.getType();
		switch (operation) {
		case "sendMessage":
			MessageSendDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageSendDTO.class);
			MessageEntity messageEntity = messageService.sendMessageInChat(message, user.getName(),
					chatId);
			if (messageEntity == null)
				return null;
			return webSocketService.sendMessage(messageEntity);

		default:
			return null;
		}
	}

	@MessageMapping("/user/{userId}")
	public WebSocketDTO operationWithUser(@Payload WebSocketDTO payload, Principal user,
			@DestinationVariable Integer userId) {
		String operation = payload.getType();
		switch (operation) {
		case "sendMessage":
			MessageSendDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageSendDTO.class);
			MessageEntity messageEntity = messageService.sendMessageToUser(message, user.getName(),
					userId);
			return webSocketService.sendMessage(messageEntity);

		default:
			return null;
		}
	}
}
