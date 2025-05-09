package ru.roms2002.messenger.server.service;

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
	private UserChatService userChatService;

	@Autowired
	private MessageUserService messageUserService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private WebSocketService webSocketService;

	@Autowired
	private FileService fileService;

	public ChatEntity createChatWith(UserEntity curUser, Integer userId) {
		ChatEntity chatEntity = new ChatEntity();
		chatEntity.setType(ChatTypeEnum.SINGLE);

		UserEntity user2 = userService.findById(userId);

		if (curUser.equals(user2) || userService.haveChatWith(curUser, user2) || user2 == null) {
			return null;
		}

		chatEntity.getUserChats().add(new UserChatEntity(curUser, chatEntity, false));
		chatEntity.getUserChats().add(new UserChatEntity(user2, chatEntity, false));

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

	public boolean makeUserAdminInChat(int userId, int chatId, boolean automated) {
		UserEntity curUser = userService.getCurrentUser();
		if (curUser == null && !automated)
			return false;

		UserChatEntity curUserChat = userChatService.findUserChat(curUser.getId(), chatId);
		if (curUserChat == null)
			return false;
		if (!automated && !curUserChat.isAdmin())
			return false;

		UserChatEntity userChatEntity = userChatService.findUserChat(userId, chatId);
		if (userChatEntity == null)
			return false;
		userChatEntity.setAdmin(true);
		userChatService.save(userChatEntity);

		UserEntity user = userService.findById(userId);
		String fullName = infoServerService.getFullName(user);
		MessageEntity infoMessage = messageService.sendInfoMessage(
				"Пользователь " + fullName + " был назначен администратором чата",
				findById(chatId));
		WebSocketDTO wsDto = webSocketService.sendMessage(infoMessage);
		webSocketService.send("/topic/chat/" + chatId, wsDto);
		return true;
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
		for (UserChatEntity userChat : chat.getUserChats())
			if (!userChat.getUser().equals(user))
				return userChat.getUser();
		return null;
	}

	public boolean addUserToChat(int userId, int chatId, boolean automated) {
		Optional<ChatEntity> tmp = chatRepository.findById(chatId);
		if (tmp.isEmpty())
			return false;
		ChatEntity chat = tmp.get();
		UserEntity user = userService.findById(userId);
		if (user == null) {
			return false;
		}
		if (userChatService.findUserChat(user.getId(), chatId) != null) {
			return false;
		}
		if (chat.getType() == ChatTypeEnum.SINGLE)
			return false;

		UserEntity curUser = userService.getCurrentUser();
		if (!automated && (userChatService.findUserChat(curUser.getId(), chatId) == null
				&& userChatService.findUserChat(curUser.getId(), chatId).isAdmin()))
			return false;

		chat.getUserChats().add(new UserChatEntity(user, chat, false));
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
		if (userChatService.findUserChat(user.getId(), chatId) == null)
			return false;
		if (chat.getType() == ChatTypeEnum.SINGLE)
			return false;

		UserEntity curUser = userService.getCurrentUser();
		if (!automated && (userChatService.findUserChat(curUser.getId(), chatId) == null
				&& userChatService.findUserChat(curUser.getId(), chatId).isAdmin()))
			return false;

		chat.getUserChats().remove(new UserChatEntity(user, chat, false));
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
		if (tmp.isPresent())
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
		fileService.deleteChatDir(chat.getId());
		chatRepository.delete(chat);
	}

	public Integer getNumberOfUsersInChat(Integer chatId) {
		ChatEntity chat = findById(chatId);
		if (chat == null)
			return 0;
		UserEntity user = userService.getCurrentUser();
		if (userChatService.findUserChat(user.getId(), chatId) == null)
			return 0;
		return chat.getUserChats().size();
	}

	public boolean leaveFromChat(Integer chatId) {
		ChatEntity chat = findById(chatId);
		if (chat.getType() != ChatTypeEnum.GROUP)
			return false;

		UserEntity user = userService.getCurrentUser();
		if (userChatService.findUserChat(user.getId(), chatId) == null)
			return false;

		chat.getUserChats().remove(new UserChatEntity(user, chat, false));
		chat = save(chat);
		user = userService.save(user);

		String fullName = infoServerService.getFullName(user);
		MessageEntity infoMessage = messageService
				.sendInfoMessage("Пользователь " + fullName + " вышел из чата", chat);
		WebSocketDTO wsDto = webSocketService.sendMessage(infoMessage);
		webSocketService.send("/topic/chat/" + chatId, wsDto);
		return true;
	}

	public boolean deleteChat(Integer chatId, boolean automated) {
		UserEntity curUser = userService.getCurrentUser();
		if (curUser == null && !automated)
			return false;

		ChatEntity chat = findById(chatId);
		if (chat == null)
			return false;
		UserChatEntity curUserChat = userChatService.findUserChat(curUser.getId(), chatId);
		if (curUserChat == null)
			return false;
		if (!automated && !curUserChat.isAdmin())
			return false;

		delete(chat);
		return true;
	}
}
