package ru.roms2002.messenger.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.MessageUserEntity;
import ru.roms2002.messenger.server.entity.MessageUserKey;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.MessageUserRepository;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Service
@Slf4j
public class MessageUserService {

	@Autowired
	private MessageUserRepository messageUserRepository;

	@Autowired
	private UserService userService;

	public void onCreateNewMessage(MessageEntity message) {
		if (message.getType() == MessageTypeEnum.INFO)
			return;
		ChatEntity chat = message.getChat();
		UserEntity sender = message.getUser();
		List<UserEntity> usersInChat = userService.getUsersInChat(chat.getId());
		for (UserEntity user : usersInChat) {
			MessageUserEntity messageUser = new MessageUserEntity(
					new MessageUserKey(message.getId(), user.getId()), message, user,
					user.equals(sender));
			messageUserRepository.save(messageUser);
		}
	}

	public MessageUserEntity findByUserIdAndMessageId(int userId, int messageId) {
		Optional<MessageUserEntity> tmp = messageUserRepository.findByMessageIdAndUserId(messageId,
				userId);
		if (tmp.isEmpty()) {
			return null;
		}
		return tmp.get();
	}

	public MessageUserEntity save(MessageUserEntity messageUserEntity) {
		return messageUserRepository.save(messageUserEntity);
	}

	public void onNewUserInChat(UserEntity user, ChatEntity chat) {
		for (MessageEntity message : chat.getMessages()) {
			if (message.getType() == MessageTypeEnum.INFO)
				continue;
			MessageUserEntity messageUser = new MessageUserEntity(
					new MessageUserKey(message.getId(), user.getId()), message, user, false);
			save(messageUser);
		}
	}

	public boolean isMessageSeen(MessageEntity message) {
		List<MessageUserEntity> messageUserList = messageUserRepository
				.findByMessageId(message.getId());
		return messageUserList.stream().filter(mu -> mu.isSeen()).count() > 1;
	}

	public void delete(MessageUserEntity messageUser) {
		messageUserRepository.delete(messageUser);
	}

	public void deleteAllMessagesFromUserInChat(int userId, int chatId) {
		messageUserRepository.deleteAllMessagesFromUserInChat(userId, chatId);
	}

	public int getNotReadByUserInChat(int userId, int chatId) {
		return messageUserRepository.countSeenByUserInChat(userId, chatId);
	}
}
