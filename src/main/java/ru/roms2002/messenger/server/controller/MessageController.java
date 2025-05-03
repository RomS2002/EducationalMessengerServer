package ru.roms2002.messenger.server.controller;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ru.roms2002.messenger.server.dto.ws.WebSocketDTO;
import ru.roms2002.messenger.server.entity.FileEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.service.MessageService;
import ru.roms2002.messenger.server.service.WebSocketService;

@RestController
@RequestMapping("/message")
public class MessageController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private WebSocketService webSocketService;

	@PostMapping("/chat/{chatId}/upload")
	public ResponseEntity<Void> uploadFileToChat(@PathVariable Integer chatId,
			@RequestParam MultipartFile file) {

		MessageEntity message = messageService.saveFileInChat(chatId, file);
		if (message == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		WebSocketDTO wsDTO = webSocketService.sendMessage(message);
		webSocketService.send("/topic/chat/" + chatId, wsDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/user/{userId}/upload")
	public ResponseEntity<Void> uploadFileToUser(@PathVariable Integer userId,
			@RequestParam MultipartFile file) {

		MessageEntity message = messageService.saveFileInSingleChat(userId, file);
		if (message == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		WebSocketDTO wsDTO = webSocketService.sendMessage(message);
		webSocketService.send("/topic/user/" + userId, wsDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/file/{messageId}")
	public ResponseEntity<Resource> downloadFileFromMessage(@PathVariable Integer messageId) {

		if (!messageService.checkRights(messageId)) {
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}

		FileEntity file = messageService.getFile(messageId);
		if (file == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		Path filePath = Paths.get(file.getUrl());
		Resource result = new FileSystemResource(filePath);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"");

		return ResponseEntity.ok().headers(headers).contentLength(filePath.toFile().length())
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(result);
	}

//    private final Logger log = LoggerFactory.getLogger(MessageController.class);
//
//    @Autowired
//    private MessageService messageService;
//
//    @GetMapping(value = "/group/{groupUrl}")
//    public WrapperMessageDTO fetchGroupMessages(@PathVariable String groupUrl) {
//        this.log.debug("Fetching messages from conversation");
//        return this.messageService.getConversationMessage(groupUrl, -1);
//    }
}
