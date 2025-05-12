package ru.roms2002.messenger.server.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.dto.MessageSendDTO;
import ru.roms2002.messenger.server.dto.ws.EditMessagePayload;
import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.service.WebSocketService;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;

@RestController
@Slf4j
public class WsController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private WebSocketService webSocketService;

	@Autowired
	private UserService userService;

	@MessageMapping("/chat/{chatId}")
	public WebSocketDTO operationWithChat(@Payload WebSocketDTO payload, Principal user,
			@DestinationVariable Integer chatId) {
		String operation = payload.getType();

		WebSocketDTO wsDto = null;
		switch (operation) {
		case "sendMessage":
			MessageSendDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageSendDTO.class);
			MessageEntity messageEntity = messageService.saveMessageInChat(message, user.getName(),
					chatId);
			if (messageEntity == null)
				return null;
			wsDto = webSocketService.sendMessage(messageEntity);
			break;
		case "readMessage":
			Integer messageId = new ObjectMapper().convertValue(payload.getPayload(),
					Integer.class);
			messageService.setMessageRead(messageId, user.getName());
			wsDto = webSocketService.sendReadNotification(messageId);
			break;
		case "editMessage":
			EditMessagePayload editMessagePayload = new ObjectMapper()
					.convertValue(payload.getPayload(), EditMessagePayload.class);
			if (!messageService.editMessage(editMessagePayload.getMessageId(),
					editMessagePayload.getMessage(), userService.findByEmail(user.getName())))
				return null;
			wsDto = webSocketService.sendEditedNotification(editMessagePayload);
			break;
		case "deleteMessage":
			Integer delMessageId = new ObjectMapper().convertValue(payload.getPayload(),
					Integer.class);
			if (!messageService.deleteMessage(delMessageId, userService.findByEmail(user.getName()),
					false))
				return null;
			wsDto = webSocketService.sendDeleteNotification(delMessageId);
			break;
		default:
			return null;
		}

		return wsDto;
	}

	@MessageMapping("/user/{userId}")
	@Transactional
	public WebSocketDTO operationWithUser(@Payload WebSocketDTO payload, Principal user,
			@DestinationVariable Integer userId) {
		String operation = payload.getType();

		WebSocketDTO wsDto = null;
		switch (operation) {
		case "sendMessage":
			MessageSendDTO message = new ObjectMapper().convertValue(payload.getPayload(),
					MessageSendDTO.class);
			MessageEntity messageEntity = messageService.saveMessageToUser(message, user.getName(),
					userId);
			wsDto = webSocketService.sendMessage(messageEntity);
			if (messageEntity.getChat().getType() != ChatTypeEnum.SELF)
				webSocketService.sendToSender(wsDto, userId);
			break;
		case "readMessage":
			Integer messageId = new ObjectMapper().convertValue(payload.getPayload(),
					Integer.class);
			messageService.setMessageRead(messageId, user.getName());
			wsDto = webSocketService.sendReadNotification(messageId);
			break;
		case "editMessage":
			EditMessagePayload editMessagePayload = new ObjectMapper()
					.convertValue(payload.getPayload(), EditMessagePayload.class);
			if (!messageService.editMessage(editMessagePayload.getMessageId(),
					editMessagePayload.getMessage(), userService.findByEmail(user.getName())))
				return null;
			wsDto = webSocketService.sendEditedNotification(editMessagePayload);
			break;
		case "deleteMessage":
			Integer delMessageId = new ObjectMapper().convertValue(payload.getPayload(),
					Integer.class);
			if (!messageService.deleteMessage(delMessageId, userService.findByEmail(user.getName()),
					false))
				return null;
			wsDto = webSocketService.sendDeleteNotification(delMessageId);
			break;
		default:
			return null;
		}

		return wsDto;
	}
}
