package ru.roms2002.messenger.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.roms2002.messenger.server.dto.WrapperMessageDTO;
import ru.roms2002.messenger.server.service.MessageService;

@RestController
@RequestMapping(value = "/messages")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:3000")
public class MessageController {

    private final Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/group/{groupUrl}")
    public WrapperMessageDTO fetchGroupMessages(@PathVariable String groupUrl) {
        this.log.debug("Fetching messages from conversation");
        return this.messageService.getConversationMessage(groupUrl, -1);
    }
}
