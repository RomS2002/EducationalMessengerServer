package ru.roms2002.messenger.server.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.roms2002.messenger.server.dto.MessageDTO;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.ws.MessagePayload;
import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.InfoServerService;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@RestController
public class WsController {

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private UserService userService;

	@Autowired
	private MessageService messageService;

	@MessageMapping("/chat/{chatId}")
	public WebSocketDTO operationWithChat(@Payload WebSocketDTO payload, Principal user,
			@DestinationVariable Integer chatId) {
		String operation = payload.getType();
		switch (operation) {
		case "sendMessage":
			MessageDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageDTO.class);
			MessageEntity messageEntity = messageService.sendMessageInChat(message, user.getName(),
					chatId);
			if (messageEntity == null)
				return null;
			return sendMessage(messageEntity);

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
			MessageDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageDTO.class);
			MessageEntity messageEntity = messageService.sendMessageToUser(message, user.getName(),
					userId);
			return sendMessage(messageEntity);

		default:
			return null;
		}
	}

	private WebSocketDTO sendMessage(MessageEntity message) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("newMessage");
		MessagePayload payload = new MessagePayload();
		payload.setChatId(message.getChat().getId());
		payload.setUserId(message.getUser().getId());
		payload.setId(message.getId());
		payload.setCreatedAt(message.getCreatedAt());
		if (message.getType() == MessageTypeEnum.TEXT) {
			payload.setMessage(message.getMessage());
			payload.setType(message.getType());
		}

		ChatEntity chat = message.getChat();
		if (chat.getType() == ChatTypeEnum.GROUP) {
			payload.setChatName(chat.getName());
		} else {
			UserEntity user = message.getUser();
			UserDetailsDTO userDetails = infoServerService
					.getUserDetailsByAdminpanelId(user.getAdminpanelId());
			payload.setChatName(userDetails.getFirstName() + " " + userDetails.getLastName());
		}

		response.setPayload(payload);
		return response;
	}
}
