package ru.roms2002.messenger.server.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.ChatRepository;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;

@Service
public class ChatService {

	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserChatJoinService userChatService;

	public ChatEntity createChatWith(UserEntity curUser, Integer userId) {
		ChatEntity chatEntity = new ChatEntity();
		chatEntity.setType(ChatTypeEnum.SINGLE);

		UserEntity user2 = userService.findById(userId);

		if (curUser.equals(user2) || userService.haveChatWith(curUser, user2) || user2 == null) {
			return null;
		}

		chatEntity.getUsers().add(curUser);
		chatEntity.getUsers().add(user2);

		chatEntity = chatRepository.save(chatEntity);
		return chatEntity;
	}

	public ChatEntity createGroupChat(String chatName) {
		ChatEntity chatEntity = new ChatEntity();
		chatEntity.setType(ChatTypeEnum.GROUP);
		chatEntity.setName(chatName);
		chatEntity = chatRepository.save(chatEntity);
		return chatEntity;
	}

	public void addUserInChat(UserEntity user, ChatEntity chat) {
		if (chat.getType() != ChatTypeEnum.GROUP) {
			return;
		}
		if (chat.getUsers().contains(user)) {
			return;
		}

		chat.getUsers().add(user);
		chatRepository.save(chat);
	}

	public void makeUserAdminInChat(int userId, ChatEntity chat) {
		UserChatEntity userChatEntity = userChatService.findUserChat(userId, chat.getId());
		if (userChatEntity == null)
			return;
		userChatEntity.setAdmin(true);
		userChatService.save(userChatEntity);
	}

	public ChatEntity findById(int chatId) {
		if (chatRepository.findById(chatId).isEmpty())
			return null;
		return chatRepository.findById(chatId).get();
	}

	public ChatEntity findChatBetween(UserEntity user, UserEntity user2) {
		Optional<ChatEntity> tmp = chatRepository.findChatBetween(user, user2);
		if (tmp.isEmpty())
			return null;
		return tmp.get();
	}

	public UserEntity getSecondUserInSingleChat(ChatEntity chat, UserEntity user) {
		for (UserEntity usr : chat.getUsers())
			if (!usr.equals(user))
				return usr;
		return null;
	}

	public boolean addUserToChat(int userId, int chatId) {
		ChatEntity chat = chatRepository.findById(chatId).get();
		UserEntity user = userService.findById(userId);
		if (chat == null || user == null) {
			return false;
		}
		if (chat.getUsers().contains(user)) {
			return false;
		}
		chat.getUsers().add(user);
		user.getChats().add(chat);
		chatRepository.save(chat);
		return true;
	}
}
