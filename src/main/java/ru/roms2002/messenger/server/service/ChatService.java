package ru.roms2002.messenger.server.service;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
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

	@Autowired
	private MessageUserService messageUserService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private WebSocketService webSocketService;

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

	public boolean addUserToChat(int userId, int chatId, boolean automated) {
		ChatEntity chat = chatRepository.findById(chatId).get();
		UserEntity user = userService.findById(userId);
		if (chat == null || user == null) {
			return false;
		}
		if (chat.getUsers().contains(user)) {
			return false;
		}
		if (chat.getType() == ChatTypeEnum.SINGLE)
			return false;

		UserEntity curUser = userService.getCurrentUser();
		if (!automated && !(chat.getUsers().contains(curUser)
				&& userChatService.findUserChat(curUser.getId(), chatId).isAdmin()))
			return false;

		chat.getUsers().add(user);
		user.getChats().add(chat);
		chatRepository.save(chat);
		messageUserService.onNewUserInChat(user, chat);

		String fullName = infoServerService.getFullName(user);
		MessageEntity infoMessage = messageService
				.sendInfoMessage("Пользователь " + fullName + " был добавлен в чат", chat);
		WebSocketDTO wsDto = webSocketService.sendMessage(infoMessage);
		webSocketService.send("/topic/chat/" + chatId, wsDto);
		return true;
	}

	public boolean removeUserFromChat(int userId, int chatId, boolean automated) {
		Optional<ChatEntity> tmp = chatRepository.findById(chatId);
		UserEntity user = userService.findById(userId);
		if (tmp.isEmpty() || user == null)
			return false;

		ChatEntity chat = tmp.get();
		if (!chat.getUsers().contains(user))
			return false;
		if (chat.getType() == ChatTypeEnum.SINGLE)
			return false;

		UserEntity curUser = userService.getCurrentUser();
		if (!automated && !(chat.getUsers().contains(curUser)
				&& userChatService.findUserChat(curUser.getId(), chatId).isAdmin()))
			return false;

		chat.getUsers().remove(user);
		user.getChats().remove(chat);
		chat = chatRepository.save(chat);

		String fullName = infoServerService.getFullName(user);
		MessageEntity infoMessage = messageService
				.sendInfoMessage("Пользователь " + fullName + " был удалён из чата", chat);
		WebSocketDTO wsDto = webSocketService.sendMessage(infoMessage);
		webSocketService.send("/topic/chat/" + chatId, wsDto);
		return true;
	}

	public void createStudgroupChat(String groupName) {
		ChatEntity studgroupChat = new ChatEntity();
		studgroupChat.setType(ChatTypeEnum.STUDGROUP);
		studgroupChat.setName(groupName);
		chatRepository.save(studgroupChat);
	}

	public ChatEntity findByStudgroupName(String groupName) {
		return chatRepository.findByStudgroupName(groupName).get();
	}

	public Optional<ChatEntity> findByDepartment(String department) {
		return chatRepository.findByDepartment(department);
	}

	public void addUserToStudgroup(UserEntity user, String groupName) {
		ChatEntity studgroupChat = findByStudgroupName(groupName);
		addUserToChat(user.getId(), studgroupChat.getId(), true);
	}

	public void createDepartmentChatIfNotExists(String department) {
		Optional<ChatEntity> tmp = findByDepartment(department);
		if (!tmp.isEmpty())
			return;

		ChatEntity departmentChat = new ChatEntity();
		departmentChat.setType(ChatTypeEnum.DEPARTMENT);
		departmentChat.setName("Кафедра «" + department + "»");
		chatRepository.save(departmentChat);
	}

	public void addUserToDepartmentChat(UserEntity user, String department) {
		createDepartmentChatIfNotExists(department);
		ChatEntity departmentChat = findByDepartment(department).get();
		addUserToChat(user.getId(), departmentChat.getId(), true);
	}

	public ChatEntity getUserSpecialChat(UserEntity user) {
		return userChatService.findSpecialChatByUser(user);
	}

	public void changeUserDepartmentChat(UserEntity user, String department) {
		createDepartmentChatIfNotExists(department);
		ChatEntity departmentChat = findByDepartment(department).get();
		removeUserFromChat(user.getId(), getUserSpecialChat(user).getId(), true);
		addUserToChat(user.getId(), departmentChat.getId(), true);
	}

	public void changeUserStudgroupChat(UserEntity user, String groupName) {
		ChatEntity studgroupChat = findByStudgroupName(groupName);
		removeUserFromChat(user.getId(), getUserSpecialChat(user).getId(), true);
		addUserToChat(user.getId(), studgroupChat.getId(), true);
	}

	public ChatEntity save(ChatEntity chat) {
		return chatRepository.save(chat);
	}

	public void delete(ChatEntity chat) {
		Iterator<MessageEntity> it = chat.getMessages().iterator();
		while (it.hasNext()) {
			MessageEntity msg = it.next();
			it.remove();
			System.out.println(messageService.deleteMessage(msg.getId(), null, true));
			System.out.println("!1");
		}
		chatRepository.delete(chat);
	}
}
