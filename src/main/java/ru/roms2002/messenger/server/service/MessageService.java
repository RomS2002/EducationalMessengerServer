package ru.roms2002.messenger.server.service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	private UserChatService userChatJoinService;

	public MessageEntity saveMessageInChat(MessageSendDTO message, String username, int chatId) {
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

		userService.updateOnline(user);
		messageEntity = messageRepository.save(messageEntity);
		messageUserService.onCreateNewMessage(messageEntity);
		return messageEntity;
	}

	public MessageEntity saveMessageToUser(MessageSendDTO message, String username, int userId) {
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

		userService.updateOnline(user);
		messageEntity = messageRepository.save(messageEntity);
		messageUserService.onCreateNewMessage(messageEntity);
		return messageEntity;
	}

	public List<MessageDTO> getMessagesInChat(int chatId, int page, int count) {
		UserEntity user = userService.getCurrentUser();
		ChatEntity chat = chatService.findById(chatId);

		if (chat == null)
			return null;
		if (!user.getChats().contains(chat))
			return null;

		Pageable countMessages = PageRequest.of(page, count);
		List<MessageEntity> messages = messageRepository.findByChatId(chatId, countMessages);
		return messages.stream().map(msg -> {
			String fullName = (msg.getType() != MessageTypeEnum.INFO)
					? infoServerService.getFullName(msg.getUser())
					: null;
			String filename = (msg.getFile() == null) ? null : msg.getFile().getFilename();
			int userId = (msg.getType() != MessageTypeEnum.INFO) ? msg.getUser().getId() : 0;

			return new MessageDTO(msg.getId(), userId, msg.getChat().getId(), msg.getType(),
					msg.getCreatedAt(), msg.getMessage(), filename, fullName, true);
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
		ChatEntity chat = userService.getChatWith(curUser, curUser);
		if (chat == null)
			chat = chatService.createChatWith(curUser, userId);
		if (chat == null) {
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
		Optional<MessageEntity> message = messageRepository.findById(messageId);
		if (message.isEmpty())
			return null;
		FileEntity file = message.get().getFile();
		return file;
	}

	public boolean checkRights(int messageId) {
		Optional<MessageEntity> message = messageRepository.findById(messageId);
		if (message.isEmpty())
			return false;
		ChatEntity chat = message.get().getChat();
		UserEntity user = userService.getCurrentUser();
		return chat.getUsers().contains(user);
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
		Optional<MessageEntity> msg = messageRepository.findById(messageId);
		if (msg.isEmpty())
			return false;
		MessageEntity editedMessageEntity = msg.get();
		if (editedMessageEntity.getType() != MessageTypeEnum.TEXT)
			return false;

		if (!editedMessageEntity.getUser().equals(curUser)) {
			return false;
		}

		editedMessageEntity.setMessage(message);
		messageRepository.save(editedMessageEntity);
		return true;
	}

	public boolean deleteMessage(Integer delMessageId, UserEntity curUser, boolean automated) {
		Optional<MessageEntity> msg = messageRepository.findById(delMessageId);
		if (msg.isEmpty())
			return false;
		MessageEntity messageEntity = msg.get();

		if (!automated) {
			if (messageEntity.getType() == MessageTypeEnum.INFO)
				return false;

			UserChatEntity userChat = userChatJoinService.findUserChat(curUser.getId(),
					messageEntity.getChat().getId());
			if (userChat == null)
				return false;

			if (!messageEntity.getUser().equals(curUser) && !userChat.isAdmin()) {
				return false;
			}
		}

		if (messageEntity.getType() == MessageTypeEnum.FILE)
			fileService.deleteFileFromMessage(messageEntity);

		messageRepository.delete(messageEntity);
		return true;
	}

	public MessageEntity sendInfoMessage(String message, ChatEntity chat) {
		MessageEntity messageEntity = new MessageEntity();
		messageEntity.setType(MessageTypeEnum.INFO);
		messageEntity.setMessage(message);
		messageEntity.setChat(chat);
		return messageRepository.save(messageEntity);
	}

	public void deleteAllMessagesFromUserInChat(UserEntity user, ChatEntity chat) {
		List<MessageEntity> messageList = chat.getMessages();
		Iterator<MessageEntity> it = messageList.iterator();
		while (it.hasNext()) {
			MessageEntity msg = it.next();
			if (msg.getType() != MessageTypeEnum.INFO && msg.getUser().getId() == user.getId())
				it.remove();
		}
		chat = chatService.save(chat);
	}
}
