package ru.roms2002.messenger.server.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/messages")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:3000")
public class MessageController {

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
