package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserChatEntity;
import ru.roms2002.messenger.server.entity.UserChatKey;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.UserChatRepository;

@Service
public class UserChatService {

//    private static final Logger log = LoggerFactory.getLogger(WsFileController.class);

	@Autowired
	private UserChatRepository userChatJoinRepository;

	public UserChatEntity findUserChat(int userId, int chatId) {
		return userChatJoinRepository.findById(new UserChatKey(chatId, userId)).get();
	}

	public UserChatEntity save(UserChatEntity userChat) {
		return userChatJoinRepository.save(userChat);
	}

	public ChatEntity findSpecialChatByUser(UserEntity user) {
		return userChatJoinRepository.findSpecialChatByUser(user);
	}
}
