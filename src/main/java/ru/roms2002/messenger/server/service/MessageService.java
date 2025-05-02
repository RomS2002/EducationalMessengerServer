package ru.roms2002.messenger.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.dto.MessageDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.MessageRepository;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Service
public class MessageService {

	@Autowired
	private UserService userService;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private ChatService chatService;

	public MessageEntity sendMessageInChat(MessageDTO message, String username, int chatId) {
		UserEntity user = userService.findByEmail(username);
		MessageEntity messageEntity = new MessageEntity();
		ChatEntity chat = chatService.findById(chatId);
		if (!chat.getUsers().contains(user)) {
			return null;
		}
		messageEntity.setChat(chat);
		messageEntity.setUser(user);
		messageEntity.setType(message.getType());
		if (message.getType() == MessageTypeEnum.TEXT)
			messageEntity.setMessage(message.getMessage());
		return messageRepository.save(messageEntity);
	}

	public MessageEntity sendMessageToUser(MessageDTO message, String username, int userId) {
		UserEntity user = userService.findByEmail(username);
		MessageEntity messageEntity = new MessageEntity();
		UserEntity user2 = userService.findById(userId);

		ChatEntity chat = chatService.findChatBetween(user, user2);
		if (chat == null) {
			chat = chatService.createChatWith(user, user2.getId());
			if (chat == null)
				return null;
		}

		messageEntity.setChat(chat);
		messageEntity.setUser(user);
		messageEntity.setType(message.getType());
		if (message.getType() == MessageTypeEnum.TEXT)
			messageEntity.setMessage(message.getMessage());
		return messageRepository.save(messageEntity);
	}

	public List<MessageEntity> getLastMessagesInChat(int chatId, int page, int count) {
		Pageable countMessages = PageRequest.of(page, count);
		return messageRepository.findByChatId(chatId, countMessages);
	}

	public MessageEntity getLastMessageInChat(int chatId) {
		List<MessageEntity> tmp = getLastMessagesInChat(chatId, 0, 1);
		if (tmp.isEmpty())
			return null;
		return tmp.get(0);
	}

//	@Autowired
//	private MessageRepository messageRepository;
//
//	@Autowired
//	private MessageService messageService;
//
//	@Autowired
//	private GroupService groupService;
//
//	@Autowired
//	private UserService userService;
//
//	@Autowired
//	private FileService fileService;
//
//	private static final String[] colorsArray = { "#FFC194", "#9CE03F", "#62C555", "#3AD079",
//			"#44CEC3", "#F772EE", "#FFAFD2", "#FFB4AF", "#FF9207", "#E3D530", "#D2FFAF", "FF5733" };
//
//	private static final Map<Integer, String> colors = new HashMap<>();
//
//	public String getRandomColor() {
//		return colorsArray[new Random().nextInt(colorsArray.length)];
//	}
//
//	public MessageEntity createAndSaveMessage(int userId, int groupId, String type, String data) {
//		MessageEntity msg = new MessageEntity(userId, groupId, type, data);
//		return messageRepository.save(msg);
//	}
//
//	public void flush() {
//		messageRepository.flush();
//	}
//
//	public MessageEntity save(MessageEntity messageEntity) {
//		return messageRepository.save(messageEntity);
//	}
//
//	public List<MessageEntity> findByGroupId(int id, int offset) {
//		List<MessageEntity> list;
//		if (offset == -1) {
//			list = messageRepository.findByGroupIdAndOffset(id, offset);
//		} else {
//			list = messageRepository.findLastMessagesByGroupId(id);
//		}
//		return list;
//	}
//
//	public void deleteAllMessagesByGroupId(int groupId) {
//		messageRepository.deleteMessagesDataByGroupId(groupId);
//	}
//
//	public MessageEntity findLastMessage(int groupId) {
//		return messageRepository.findLastMessageByGroupId(groupId);
//	}
//
//	public int findLastMessageIdByGroupId(int groupId) {
//		return messageRepository.findLastMessageIdByGroupId(groupId);
//	}
//
//	/**
//	 * Create a MessageDTO Sent with user's initials
//	 *
//	 * @param id       of the message saved in DB
//	 * @param userId   int value for user ID
//	 * @param date     String of message sending date
//	 * @param group_id int value for group ID
//	 * @param message  string for the message content
//	 * @return a {@link MessageDTO}
//	 */
//	public MessageDTO createMessageDTO(int id, String type, int userId, String date, int group_id,
//			String message) {
//		colors.putIfAbsent(userId, getRandomColor());
//
//		String username = userService.findUsernameById(userId);
//		String fileUrl = "";
//		String[] arr = username.split(",");
//		String initials = arr[0].substring(0, 1).toUpperCase()
//				+ arr[1].substring(0, 1).toUpperCase();
//		String sender = StringUtils.capitalize(arr[0]) + " " + StringUtils.capitalize(arr[1]);
//		if (type.equals(MessageTypeEnum.FILE.toString())) {
//			FileEntity fileEntity = fileService.findByFkMessageId(id);
//			fileUrl = fileEntity.getUrl();
//		}
//		return new MessageDTO(id, type, message, userId, group_id, null, sender, date, initials,
//				colors.get(userId), fileUrl, userId == id);
//	}
//
//	public static String createUserInitials(String firstAndLastName) {
//		String[] names = firstAndLastName.split(",");
//		return names[0].substring(0, 1).toUpperCase() + names[1].substring(0, 1).toUpperCase();
//	}
//
//	@Transactional
//	public List<Integer> createNotificationList(int userId, String groupUrl) {
//		int groupId = groupService.findGroupByUrl(groupUrl);
//		List<Integer> toSend = new ArrayList<>();
//		Optional<ChatEntity> optionalGroupEntity = groupService.findById(groupId);
//		if (optionalGroupEntity.isPresent()) {
//			ChatEntity groupEntity = optionalGroupEntity.get();
//			groupEntity.getUserEntities().forEach(userEntity -> toSend.add(userEntity.getId()));
//		}
//		return toSend;
//	}
//
//	public NotificationDTO createNotificationDTO(MessageEntity msg) {
//		String groupUrl = groupService.getGroupUrlById(msg.getGroup_id());
//		NotificationDTO notificationDTO = new NotificationDTO();
//		notificationDTO.setGroupId(msg.getGroup_id());
//		notificationDTO.setGroupUrl(groupUrl);
//		if (msg.getType().equals(MessageTypeEnum.TEXT.toString())) {
//			notificationDTO.setType(MessageTypeEnum.TEXT);
//			notificationDTO.setMessage(msg.getMessage());
//		}
//		if (msg.getType().equals(MessageTypeEnum.FILE.toString())) {
//			FileEntity fileEntity = fileService.findByFkMessageId(msg.getId());
//			notificationDTO.setType(MessageTypeEnum.FILE);
//			notificationDTO.setMessage(msg.getMessage());
//			notificationDTO.setFileUrl(fileEntity.getUrl());
//			notificationDTO.setFileName(fileEntity.getFilename());
//		}
//		notificationDTO.setFromUserId(msg.getUser_id());
//		notificationDTO.setLastMessageDate(msg.getCreatedAt().toString());
//		notificationDTO.setSenderName(userService.findFirstNameById(msg.getUser_id()));
//		notificationDTO.setMessageSeen(false);
//		return notificationDTO;
//	}
//
//	public MessageDTO createNotificationMessageDTO(MessageEntity msg, int userId) {
//		String groupUrl = groupService.getGroupUrlById(msg.getGroup_id());
//		String firstName = userService.findFirstNameById(msg.getUser_id());
//		String initials = userService.findUsernameById(msg.getUser_id());
//		MessageDTO messageDTO = new MessageDTO();
//		messageDTO.setId(msg.getId());
//		if (msg.getType().equals(MessageTypeEnum.FILE.toString())) {
//			String url = fileService.findFileUrlByMessageId(msg.getId());
//			messageDTO.setFileUrl(url);
//		}
//		messageDTO.setType(msg.getType());
//		messageDTO.setMessage(msg.getMessage());
//		messageDTO.setUserId(msg.getUser_id());
//		messageDTO.setGroupUrl(groupUrl);
//		messageDTO.setGroupId(msg.getGroup_id());
//		messageDTO.setSender(firstName);
//		messageDTO.setTime(msg.getCreatedAt().toString());
//		messageDTO.setInitials(createUserInitials(initials));
//		messageDTO.setColor(colors.get(msg.getUser_id()));
//		messageDTO.setMessageSeen(msg.getUser_id() == userId);
//		return messageDTO;
//	}
//
//	/**
//	 * Return history of group discussion
//	 *
//	 * @param url The group url to map
//	 * @return List of message
//	 */
//	public WrapperMessageDTO getConversationMessage(String url, int messageId) {
//		WrapperMessageDTO wrapper = new WrapperMessageDTO();
//		if (url != null) {
//			List<MessageDTO> messageDTOS = new ArrayList<>();
//			ChatEntity group = groupService.getGroupByUrl(url);
//			List<MessageEntity> newMessages = messageService.findByGroupId(group.getId(),
//					messageId);
//			if (newMessages != null) {
//				newMessages
//						.forEach(msg -> messageDTOS.add(messageService.createMessageDTO(msg.getId(),
//								msg.getType(), msg.getUser_id(), msg.getCreatedAt().toString(),
//								msg.getGroup_id(), msg.getMessage())));
//			}
////            wrapper.setLastMessage(afterMessages != null && afterMessages.isEmpty());
//			wrapper.setLastMessage(true);
//			wrapper.setMessages(messageDTOS);
//			wrapper.setGroupName(group.getName());
//			return wrapper;
//		}
//		return null;
//	}
}
