package ru.roms2002.messenger.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ru.roms2002.messenger.server.dto.AddRemoveUserDTO;
import ru.roms2002.messenger.server.dto.ChatDTO;
import ru.roms2002.messenger.server.dto.MessageDTO;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.UserInListDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.ChatService;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.utils.StaticVariable;

@RestController
public class ApiController {

	@Autowired
	private UserService userService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private MessageService messageService;

	@GetMapping("/chats")
	public List<ChatDTO> getChats() {
		return userService.getChatList();
	}

	@GetMapping("user-profile/{id}")
	public UserDetailsDTO getUserInfo(@PathVariable Integer id) {
		return userService.getUserInfo(id);
	}

	@GetMapping("my-profile")
	public UserDetailsDTO getMyInfo() {
		UserEntity user = userService.getCurrentUser();
		int id = user.getId();
		return userService.getUserInfo(id);
	}

	@GetMapping("/users")
	public List<UserInListDTO> findUsers(@RequestParam("last-name") String lastName) {
		return userService.findUsers(lastName);
	}

	@PostMapping("/upload-avatar")
	public ResponseEntity<Void> uploadAvatar(@RequestParam MultipartFile image) {
		return userService.uploadAvatar(image) ? new ResponseEntity<>(HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@PostMapping("/chat/add-user")
	public ResponseEntity<Void> addUserInChat(@RequestBody AddRemoveUserDTO addUserDTO) {
		if (!chatService.addUserToChat(addUserDTO.getUserId(), addUserDTO.getChatId(), false)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/chat/remove-user")
	public ResponseEntity<Void> removeUserFromChat(@RequestBody AddRemoveUserDTO removeUserDTO) {
		if (!chatService.removeUserFromChat(removeUserDTO.getUserId(), removeUserDTO.getChatId(),
				false)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/chat/provide-admin")
	public ResponseEntity<Void> provideToAdmin(@RequestBody AddRemoveUserDTO userChatDTO) {
		if (!chatService.makeUserAdminInChat(userChatDTO.getUserId(), userChatDTO.getChatId(),
				false)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/chat/{chatId}/delete")
	public ResponseEntity<Void> deleteChat(@PathVariable Integer chatId) {
		if (!chatService.deleteChat(chatId, false)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/chat/create-group")
	public ResponseEntity<Void> createGroupChat(@RequestBody String chatName) {

		ChatEntity chat = chatService.createGroupChat(chatName);
		UserEntity user = userService.getCurrentUser();
		chatService.addUserToChat(user.getId(), chat.getId(), true);
		chatService.makeUserAdminInChat(user.getId(), chat.getId(), true);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/chat/{chatId}/messages")
	public List<MessageDTO> getMessages(@PathVariable Integer chatId, @RequestParam Integer page) {
		return messageService.getMessagesInChat(chatId, page, StaticVariable.MESSAGE_PAGE_SIZE);
	}

	@GetMapping("/get-online-status/{userId}")
	public String getOnline(@PathVariable Integer userId) {
		UserEntity user = userService.findById(userId);
		if (user == null)
			return "NoUser";
		return (userService.checkOnline(user)) ? "В сети" : "Не в сети";
	}

	@GetMapping("/chat/{chatId}/user-number")
	public Integer getNumberOfUsersInChat(@PathVariable Integer chatId) {
		return chatService.getNumberOfUsersInChat(chatId);
	}

	@GetMapping("/my-id")
	public Integer getMyId() {
		UserEntity user = userService.getCurrentUser();
		return user.getId();
	}

	@GetMapping("/get-singlechat-id/{userId}")
	public Integer getSinglechatId(@PathVariable Integer userId) {
		ChatEntity chat = userService.getChatWith(userService.getCurrentUser(),
				userService.findById(userId));
		if (chat == null)
			return 0;
		return chat.getId();
	}

	@GetMapping("/get-selfchat-id")
	public Integer getSelfchatId() {
		ChatEntity chat = chatService.getSelfChat(userService.getCurrentUser());
		if (chat == null)
			return 0;
		return chat.getId();
	}

	@PostMapping("/chat/{chatId}/leave")
	public ResponseEntity<Void> leaveFromChat(@PathVariable Integer chatId) {
		if (!chatService.leaveFromChat(chatId))
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
