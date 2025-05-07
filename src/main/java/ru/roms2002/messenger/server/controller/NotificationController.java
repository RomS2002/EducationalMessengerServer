package ru.roms2002.messenger.server.controller;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.roms2002.messenger.server.dto.notification.ChangeDepartmentDTO;
import ru.roms2002.messenger.server.dto.notification.ChangeRoleDTO;
import ru.roms2002.messenger.server.dto.notification.ChangeStudgroupDTO;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.ChatService;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;
import ru.roms2002.messenger.server.utils.enums.RoleEnum;

@RestController
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	private UserService userService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private MessageService messageService;

	@PostMapping("/blocked")
	public void notifyBlocked(@RequestBody Integer userId) {
		UserEntity user = userService.findByAdminpanelId(userId);
		userService.destroyAllWsSessions(user.getEmail());
	}

	@PostMapping("/new-group")
	public void sendNewGroupNotification(@RequestBody String groupName) {
		chatService.createStudgroupChat(groupName);
	}

	@PostMapping("/delete-user")
	public void sendDeleteUserNotification(@RequestBody Integer userId) {
		this.deleteUser(userId);
	}

	@PostMapping("/change-group")
	public void sendChangeGroupNotification(@RequestBody ChangeStudgroupDTO dto) {
		UserEntity user = userService.findByAdminpanelId(dto.getUserId());
		if (user == null)
			return;
		chatService.changeUserStudgroupChat(user, dto.getGroupName());
	}

	@PostMapping("/change-role")
	public void sendChangeRoleNotification(@RequestBody ChangeRoleDTO dto) {
		UserEntity user = userService.findByAdminpanelId(dto.getUserId());
		if (user == null)
			return;
		if (dto.getRole().equals("Студент")) {
			chatService.changeUserStudgroupChat(user, dto.getGroupName());
			user.setRole(RoleEnum.STUDENT);
		}
		if (dto.getRole().equals("Преподаватель")) {
			chatService.changeUserDepartmentChat(user, dto.getDepartment());
			user.setRole(RoleEnum.PROFESSOR);
		}
		userService.save(user);
	}

	@PostMapping("/change-department")
	public void sendChangeDepartmentNotification(@RequestBody ChangeDepartmentDTO dto) {
		UserEntity user = userService.findByAdminpanelId(dto.getUserId());
		if (user == null)
			return;
		chatService.changeUserDepartmentChat(user, dto.getDepartment());
	}

	@PostMapping("/delete-group")
	public void sendDeleteGroupNotification(@RequestBody String groupName) {
		ChatEntity studgroupChat = chatService.findByStudgroupName(groupName);
		Iterator<UserEntity> it = studgroupChat.getUsers().iterator();
		while (it.hasNext()) {
			UserEntity userInStudgroup = it.next();
			this.deleteUser(userInStudgroup.getAdminpanelId());
			it.remove();
		}
		chatService.delete(studgroupChat);
	}

	private void deleteUser(int adminPanelId) {
		UserEntity user = userService.findByAdminpanelId(adminPanelId);
		if (user == null)
			return;
		userService.destroyAllWsSessions(user.getEmail());
		Iterator<ChatEntity> it = user.getChats().iterator();
		while (it.hasNext()) {
			ChatEntity chat = it.next();
			if (chat.getType() == ChatTypeEnum.SINGLE) {
				chatService.delete(chat);
			} else {
				messageService.deleteAllMessagesFromUserInChat(user, chat);
				chat = chatService.findById(chat.getId());
				System.out.println(chat.getId());
				System.out.println(chat.getMessages());
				System.out.println("2) " + chat.getUserChats().get(0).getUser().getId());
				if (chat.getUsers().size() == 1 && chat.getType() != ChatTypeEnum.STUDGROUP) {
					chatService.delete(chat);
				} else {
					chatService.removeUserFromChat(user.getId(), chat.getId(), true);
					chat.getUsers().remove(user);
					chat = chatService.save(chat);
				}
			}
			it.remove();
		}
		user = userService.save(user);
		userService.delete(user);
	}
}
