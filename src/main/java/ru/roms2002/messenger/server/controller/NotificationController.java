package ru.roms2002.messenger.server.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.UserService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	private UserService userService;

	@PostMapping("/blocked")
	public void notifyBlocked(@RequestBody Integer userId) {
		UserEntity user = userService.findByAdminpanelId(userId);
		System.out.println(user.getEmail());
		if (userService.getWsSessions().get(user.getEmail()) == null)
			return;

		for (WebSocketSession session : userService.getWsSessions().get(user.getEmail())) {
			try {
				session.close(CloseStatus.NOT_ACCEPTABLE);
			} catch (IOException e) {

			}
		}
	}
}
