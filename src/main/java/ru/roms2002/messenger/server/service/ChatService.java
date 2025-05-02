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

//	private static final Logger log = LoggerFactory.getLogger(GroupService.class);
//
//	@Autowired
//	private GroupRepository groupRepository;
//
//	@Autowired
//	private UserService userService;
//
//	@Autowired
//	private GroupUserJoinService groupUserJoinService;
//
//	public int findGroupByUrl(String url) {
//		return groupRepository.findGroupByUrl(url);
//	}
//
//	public ChatEntity getGroupByUrl(String url) {
//		return groupRepository.getGroupByUrl(url);
//	}
//
//	public List<Integer> getAllUsersIdByGroupUrl(String groupUrl) {
//		int groupId = groupRepository.findGroupByUrl(groupUrl);
//		List<UserChat> users = groupUserJoinService.findAllByGroupId(groupId);
//		return users.stream().map(UserChat::getUserId).collect(Collectors.toList());
//	}
//
//	public String getGroupName(String url) {
//		return groupRepository.getGroupEntitiesBy(url);
//	}
//
//	public String getGroupUrlById(int id) {
//		return groupRepository.getGroupUrlById(id);
//	}
//
//	public GroupMemberDTO addUserToConversation(int userId, int groupId) {
//		Optional<ChatEntity> groupEntity = groupRepository.findById(groupId);
//		if (groupEntity.isPresent()
//				&& groupEntity.orElse(null).getGroupTypeEnum().equals(ChatTypeEnum.SINGLE)) {
//			log.info("Cannot add user in a single conversation");
//			return new GroupMemberDTO();
//		}
//		UserEntity user = userService.findById(userId);
//		UserChat groupUser = new UserChat();
//		groupUser.setGroupUsers(groupEntity.orElse(null));
//		groupUser.setUserEntities(user);
//		groupUser.setGroupId(groupId);
//		groupUser.setUserId(userId);
//		groupUser.setRole(0);
//		UserChat saved = groupUserJoinService.save(groupUser);
//		assert groupEntity.orElse(null) != null;
//		groupEntity.orElse(null).getGroupUsers().add(saved);
//		groupRepository.save(groupEntity.orElse(null));
//		return new GroupMemberDTO(user.getId(), user.getFirstName(), user.getLastName(), false);
//	}
//
//	public ChatEntity createGroup(int userId, String name) {
//		UserChat groupUser = new UserChat();
//		ChatEntity group = new ChatEntity(name);
//		group.setName(name);
//		group.setUrl(UUID.randomUUID().toString());
//		group.setGroupTypeEnum(ChatTypeEnum.GROUP);
//		ChatEntity savedGroup = groupRepository.save(group);
//		UserEntity user = userService.findById(userId);
//		ChatRoleKey groupRoleKey = new ChatRoleKey();
//		groupRoleKey.setUserId(userId);
//		groupRoleKey.setGroupId(savedGroup.getId());
//		groupUser.setGroupId(savedGroup.getId());
//		groupUser.setUserId(userId);
//		groupUser.setRole(1);
//		groupUser.setUserEntities(user);
//		groupUser.setGroupUsers(group);
//		groupUserJoinService.save(groupUser);
//		return savedGroup;
//	}
//
//	public Optional<ChatEntity> findById(int groupId) {
//		return groupRepository.findById(groupId);
//	}
//
//	public void createConversation(int id1, int id2) {
//		ChatEntity groupEntity = new ChatEntity();
//		groupEntity.setName(null);
//		groupEntity.setUrl(UUID.randomUUID().toString());
//		groupEntity.setGroupTypeEnum(ChatTypeEnum.SINGLE);
//		ChatEntity savedGroup = groupRepository.save(groupEntity);
//
//		UserEntity user1 = userService.findById(id1);
//		UserEntity user2 = userService.findById(id2);
//
//		UserChat groupUser1 = new UserChat();
//		groupUser1.setGroupId(savedGroup.getId());
//		groupUser1.setUserId(id1);
//
//		groupUser1.setRole(0);
//		groupUser1.setUserEntities(user1);
//		groupUser1.setGroupUsers(groupEntity);
//
//		UserChat groupUser2 = new UserChat();
//		groupUser2.setUserId(savedGroup.getId());
//		groupUser2.setGroupId(id2);
//		groupUser2.setRole(0);
//		groupUser2.setUserEntities(user2);
//		groupUser2.setGroupUsers(groupEntity);
//		groupUserJoinService.saveAll(Arrays.asList(groupUser1, groupUser2));
//	}
}
