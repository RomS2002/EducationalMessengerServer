package ru.roms2002.messenger.server.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import ru.roms2002.messenger.server.service.ChatService;
import ru.roms2002.messenger.server.service.InfoServerService;

@Component
public class CreateDepartmentChats {

	@Autowired
	private ChatService chatService;

	@Autowired
	private InfoServerService infoServerService;

	@PostConstruct
	public void createDepartmentChats() {

		List<String> departments = infoServerService.getDepartments();
		for (String department : departments)
			chatService.createDepartmentChatIfNotExists(department);
	}
}
