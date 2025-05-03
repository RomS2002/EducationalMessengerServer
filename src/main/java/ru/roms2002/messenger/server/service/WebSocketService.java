package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.dto.UserDetailsDTO;
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
	private InfoServerService infoServerService;

	public WebSocketDTO sendMessage(MessageEntity message) {
		WebSocketDTO response = new WebSocketDTO();
		response.setType("newMessage");
		MessagePayload payload = new MessagePayload();
		payload.setId(message.getId());
		payload.setChatId(message.getChat().getId());
		payload.setUserId(message.getUser().getId());
		payload.setCreatedAt(message.getCreatedAt());
		payload.setType(message.getType());
		if (message.getType() == MessageTypeEnum.TEXT) {
			payload.setMessage(message.getMessage());
		} else {
			payload.setFilename(message.getFile().getFilename());
		}
		payload.setSeen(false);

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

	public void send(String destination, WebSocketDTO payload) {
		messagingTemplate.convertAndSend(destination, payload);
	}
}
