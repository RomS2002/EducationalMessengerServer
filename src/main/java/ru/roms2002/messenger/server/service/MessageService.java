package ru.roms2002.messenger.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.dto.MessageDTO;
import ru.roms2002.messenger.server.dto.MessageSendDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.FileEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.MessageUserEntity;
import ru.roms2002.messenger.server.entity.UserChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.MessageRepository;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Service
@Slf4j
public class MessageService {

	@Autowired
	private UserService userService;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private ChatService chatService;

	@Autowired
	private FileService fileService;

	@Autowired
	private MessageUserService messageUserService;

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private UserChatService userChatService;

	public MessageEntity saveMessageInChat(MessageSendDTO message, String username, int chatId) {
		UserEntity user = userService.findByEmail(username);
		MessageEntity messageEntity = new MessageEntity();
		ChatEntity chat = chatService.findById(chatId);
		if (userChatService.findUserChat(user.getId(), chatId) == null) {
			return null;
		}
		messageEntity.setChat(chat);
		messageEntity.setUser(user);
		messageEntity.setType(message.getType());
		if (message.getType() == MessageTypeEnum.TEXT)
			messageEntity.setMessage(message.getMessage());

		userService.updateOnline(user);
		messageEntity = save(messageEntity);
		messageUserService.onCreateNewMessage(messageEntity);
		return messageEntity;
	}

	public MessageEntity saveMessageToUser(MessageSendDTO message, String username, int userId) {
		UserEntity user = userService.findByEmail(username);
		MessageEntity messageEntity = new MessageEntity();
		UserEntity user2 = userService.findById(userId);

		ChatEntity chat;
		if (user.equals(user2)) {
			chat = chatService.getSelfChat(user);
		} else {
			chat = chatService.findChatBetween(user, user2);
		}

		if (chat == null) {
			if (user.equals(user2))
				chat = chatService.createSelfChat(user);
			else
				chat = chatService.createChatWith(user, user2.getId());
			if (chat == null)
				return null;
		}

		messageEntity.setChat(chat);
		messageEntity.setUser(user);
		messageEntity.setType(message.getType());
		if (message.getType() == MessageTypeEnum.TEXT)
			messageEntity.setMessage(message.getMessage());

		userService.updateOnline(user);
		messageEntity = save(messageEntity);
		messageUserService.onCreateNewMessage(messageEntity);
		return messageEntity;
	}

	public List<MessageDTO> getMessagesInChat(int chatId, int page, int count) {
		UserEntity user = userService.getCurrentUser();
		ChatEntity chat = chatService.findById(chatId);

		if (chat == null)
			return null;
		if (userChatService.findUserChat(user.getId(), chatId) == null)
			return null;

		Pageable countMessages = PageRequest.of(page, count);
		List<MessageEntity> messages = messageRepository.findByChatId(chatId, countMessages);

		Map<Integer, String> fullNames = new HashMap<>();

		return messages.stream().map(msg -> {
			String fullNameSender;
			if (msg.getType() == MessageTypeEnum.INFO)
				fullNameSender = null;
			else if (fullNames.containsKey(msg.getUser().getId()))
				fullNameSender = fullNames.get(msg.getUser().getId());
			else {
				fullNameSender = infoServerService.getFullName(msg.getUser());
				fullNames.put(msg.getUser().getId(), fullNameSender);
			}

			String filename = (msg.getFile() == null) ? null : msg.getFile().getFilename();
			int userId = (msg.getType() != MessageTypeEnum.INFO) ? msg.getUser().getId() : 0;

			boolean seen = messageUserService.isMessageSeen(msg);
			boolean seenByMe = false;

			if (msg.getType() != MessageTypeEnum.INFO && msg.getUser().getId() != user.getId()) {
				seenByMe = messageUserService.findByUserIdAndMessageId(user.getId(), msg.getId())
						.isSeen();
			}

			return new MessageDTO(msg.getId(), userId, msg.getChat().getId(), msg.getType(),
					msg.getCreatedAt(), msg.getMessage(), filename, fullNameSender, null, seen,
					seenByMe);
		}).toList();
	}

	public MessageEntity getLastMessageInChat(int chatId) {
		Pageable countMessages = PageRequest.of(0, 1);
		List<MessageEntity> tmp = messageRepository.findByChatId(chatId, countMessages);
		if (tmp.isEmpty())
			return null;
		int i = 1;
		while (tmp.get(0).getType() == MessageTypeEnum.INFO) {
			countMessages = PageRequest.of(i, 1);
			tmp = messageRepository.findByChatId(chatId, countMessages);
			if (tmp.isEmpty())
				return null;
			i++;
		}
		return tmp.get(0);
	}

	public MessageEntity saveFileInSingleChat(Integer userId, MultipartFile file) {

		UserEntity curUser = userService.getCurrentUser();
		UserEntity user = userService.findById(userId);
		ChatEntity chat;
		if (user.equals(curUser)) {
			chat = chatService.getSelfChat(user);
		} else {
			chat = chatService.findChatBetween(user, curUser);
		}
		if (chat == null) {
			if (user.equals(curUser))
				chat = chatService.createSelfChat(user);
			else
				chat = chatService.createChatWith(user, curUser.getId());
			if (chat == null)
				return null;
		}
		return saveFileInChat(chat.getId(), file);
	}

	public MessageEntity saveFileInChat(int chatId, MultipartFile file) {
		MessageEntity message = new MessageEntity();
		message.setType(MessageTypeEnum.FILE);
		message.setUser(userService.getCurrentUser());

		ChatEntity chat = chatService.findById(chatId);
		if (chat == null)
			return null;
		message.setChat(chat);

		FileEntity fileEntity = new FileEntity();
		fileEntity.setFilename(file.getOriginalFilename());
		fileEntity.setMessage(message);
		String url = "./uploads/" + chatId;
		String filename = UUID.randomUUID().toString();
		fileEntity.setUrl(url + "/" + filename);
		if (!fileService.saveFileOnDisk(url, filename, file)) {
			return null;
		}
		message.setFile(fileEntity);
		fileEntity = fileService.save(fileEntity);
		message = fileEntity.getMessage();
		messageUserService.onCreateNewMessage(message);
		return message;
	}

	public FileEntity getFile(int messageId) {
		MessageEntity message = findById(messageId);
		if (message == null)
			return null;
		FileEntity file = message.getFile();
		return file;
	}

	public boolean checkRights(int messageId) {
		MessageEntity message = findById(messageId);
		if (message == null)
			return false;
		ChatEntity chat = message.getChat();
		UserEntity user = userService.getCurrentUser();
		return userChatService.findUserChat(chat.getId(), user.getId()) == null;
	}

	public void setMessageRead(int messageId, String username) {
		UserEntity user = userService.findByEmail(username);
		MessageUserEntity messageUser = messageUserService.findByUserIdAndMessageId(user.getId(),
				messageId);
		if (messageUser == null)
			return;
		messageUser.setSeen(true);
		messageUserService.save(messageUser);
	}

	public boolean editMessage(int messageId, String message, UserEntity curUser) {
		MessageEntity editedMessageEntity = findById(messageId);
		if (editedMessageEntity == null)
			return false;
		if (editedMessageEntity.getType() != MessageTypeEnum.TEXT)
			return false;

		if (!editedMessageEntity.getUser().equals(curUser)) {
			return false;
		}

		editedMessageEntity.setMessage(message);
		save(editedMessageEntity);
		return true;
	}

	public boolean deleteMessage(Integer delMessageId, UserEntity curUser, boolean automated) {
		MessageEntity messageEntity = findById(delMessageId);
		if (messageEntity == null)
			return false;

		if (!automated) {
			if (messageEntity.getType() == MessageTypeEnum.INFO)
				return false;

			UserChatEntity userChat = userChatService.findUserChat(curUser.getId(),
					messageEntity.getChat().getId());
			if (userChat == null)
				return false;

			if (!messageEntity.getUser().equals(curUser) && !userChat.isAdmin()) {
				return false;
			}
		}

		if (messageEntity.getType() == MessageTypeEnum.FILE)
			fileService.deleteFileFromMessage(messageEntity);

		delete(messageEntity);
		return true;
	}

	public MessageEntity sendInfoMessage(String message, ChatEntity chat) {
		MessageEntity messageEntity = new MessageEntity();
		messageEntity.setType(MessageTypeEnum.INFO);
		messageEntity.setMessage(message);
		messageEntity.setChat(chat);
		return save(messageEntity);
	}

	@CacheEvict("messages")
	public void delete(MessageEntity message) {
		messageRepository.delete(message);
	}

	@CacheEvict("messages")
	public void deleteAllMessagesFromUserInChat(UserEntity user, ChatEntity chat) {
		messageRepository.deleteAllMessagesFromUserInChat(user.getId(), chat.getId());
	}

	@CachePut("messages")
	public MessageEntity save(MessageEntity message) {
		return messageRepository.save(message);
	}

	public MessageEntity findById(int chatId) {
		Optional<MessageEntity> tmp = messageRepository.findById(chatId);
		if (tmp.isEmpty())
			return null;
		return tmp.get();
	}
}
