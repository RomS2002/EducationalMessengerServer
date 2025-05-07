package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.ws.EditMessagePayload;
import ru.roms2002.messenger.server.dto.ws.MessagePayload;
import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Service
public class WebSocketService {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private UserService userService;

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private WebSocketService webSocketService;

	public WebSocketDTO sendMessage(MessageEntity message) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("newMessage");
		MessagePayload payload = new MessagePayload();
		payload.setId(message.getId());
		payload.setChatId(message.getChat().getId());

		if (message.getType() != MessageTypeEnum.INFO)
			payload.setSenderId(message.getUser().getId());

		payload.setCreatedAt(message.getCreatedAt());
		payload.setType(message.getType());
		if (message.getType() != MessageTypeEnum.FILE) {
			payload.setMessage(message.getMessage());
		} else {
			payload.setFilename(message.getFile().getFilename());
		}
		payload.setSeen(false);

		ChatEntity chat = message.getChat();
		if (chat.getType() != ChatTypeEnum.SINGLE) {
			payload.setSenderName(chat.getName());
		} else {
			UserEntity user = message.getUser();
			if (user != null) {
				UserDetailsDTO userDetails = infoServerService
						.getUserDetailsByAdminpanelId(user.getAdminpanelId());
				payload.setSenderName(userDetails.getFirstName() + " " + userDetails.getLastName());
			}
		}

		response.setPayload(payload);
		return response;
	}

	public void send(String destination, WebSocketDTO payload) {
		messagingTemplate.convertAndSend(destination, payload);
	}

	public void sendToSender(WebSocketDTO payload, int userId) {
		WebSocketDTO wsDto = SerializationUtils.clone(payload);
		MessagePayload messagePayload = (MessagePayload) wsDto.getPayload();
		UserEntity user = userService.findById(userId);
		UserDetailsDTO userDetails = infoServerService
				.getUserDetailsByAdminpanelId(user.getAdminpanelId());
		messagePayload.setSenderName(userDetails.getFirstName() + " " + userDetails.getLastName());
		webSocketService.send("/topic/user/" + messagePayload.getSenderId(), wsDto);
	}

	public WebSocketDTO sendReadNotification(Integer messageId) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("wasRead");
		response.setPayload(messageId);
		return response;
	}

	public WebSocketDTO sendEditedNotification(EditMessagePayload editMessagePayload) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("wasEdited");
		response.setPayload(editMessagePayload);
		return response;
	}

	public WebSocketDTO sendDeleteNotification(Integer delMessageId) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("wasDeleted");
		response.setPayload(delMessageId);
		return response;
	}
}
