package ru.roms2002.messenger.server.controller;

import com.google.gson.Gson;

import ru.roms2002.messenger.server.dto.*;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.MessageUserEntity;
import ru.roms2002.messenger.server.service.*;
import ru.roms2002.messenger.server.utils.MessageTypeEnum;
import ru.roms2002.messenger.server.utils.RtcActionEnum;
import ru.roms2002.messenger.server.utils.TransportActionEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WsController {

    @Autowired
    private RoomCacheService roomCacheService;

    private final Logger log = LoggerFactory.getLogger(WsController.class);

    private final Map<String, ArrayList<Integer>> usersIndexedByRoomId = new HashMap<>();

    @Autowired
    private GroupService groupService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GroupUserJoinService groupUserJoinService;

    @Autowired
    private UserSeenMessageService seenMessageService;

    @GetMapping(value = "/room/ensure-room-exists/{groupUrl}")
    public Boolean ensureCallRoomExists(@PathVariable String groupUrl) {
        if (StringUtils.hasLength(groupUrl)) {
            return roomCacheService.getRoomByKey(groupUrl) != null;
        }
        return false;
    }

    @MessageMapping("/message")
    public void mainChannel(InputTransportDTO dto, @Header("simpSessionId") String sessionId) {
        TransportActionEnum action = dto.getAction();
        switch (action) {
            case SEND_GROUP_MESSAGE:
                this.getAndSaveMessage(dto.getUserId(), dto.getGroupUrl(), dto.getMessage());
                break;
//            case FETCH_GROUP_MESSAGES:
//                if (!dto.getGroupUrl().equals("")) {
//                    int groupId = groupService.findGroupByUrl(dto.getGroupUrl());
//                    if (dto.getGroupUrl().equals("") || groupUserJoinService.checkIfUserIsAuthorizedInGroup(dto.getUserId(), groupId)) {
//                        break;
//                    }
//                    OutputTransportDTO resMessages = new OutputTransportDTO();
//                    if (dto.getMessageId() == -1) {
//                        resMessages.setAction(TransportActionEnum.FETCH_GROUP_MESSAGES);
//                    } else {
//                        resMessages.setAction(TransportActionEnum.ADD_CHAT_HISTORY);
//                    }
//                    resMessages.setObject(messages);
//                    this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), resMessages);
//                }
//                break;
            case MARK_MESSAGE_AS_SEEN:
                if (!"".equals(dto.getGroupUrl())) {
                    int messageId = messageService.findLastMessageIdByGroupId(groupService.findGroupByUrl(dto.getGroupUrl()));
                    MessageUserEntity messageUserEntity = seenMessageService.findByMessageId(messageId, dto.getUserId());
                    if (messageUserEntity == null) break;
                    messageUserEntity.setSeen(true);
                    seenMessageService.saveMessageUserEntity(messageUserEntity);
                }
                break;
            case LEAVE_GROUP:
                if (!dto.getGroupUrl().equals("")) {
                    log.info("User id {} left group {}", dto.getUserId(), dto.getGroupUrl());
                    int groupId = groupService.findGroupByUrl(dto.getGroupUrl());
                    groupUserJoinService.removeUserFromConversation(dto.getUserId(), groupId);

                    String groupName = groupService.getGroupName(dto.getGroupUrl());
                    LeaveGroupDTO leaveGroupDTO = new LeaveGroupDTO();
                    leaveGroupDTO.setGroupUrl(dto.getGroupUrl());
                    leaveGroupDTO.setGroupName(groupName);
                    OutputTransportDTO leaveResponse = new OutputTransportDTO();
                    leaveResponse.setAction(TransportActionEnum.LEAVE_GROUP);
                    leaveResponse.setObject(leaveGroupDTO);
                    this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), leaveResponse);
                } else {
                    log.warn("User cannot left group because groupUrl is empty");
                }
                break;
            case CHECK_EXISTING_CALL:
                OutputTransportDTO outputTransportDTO = new OutputTransportDTO();
                for (String key : usersIndexedByRoomId.keySet()) {
                    if (key.contains(dto.getGroupUrl())) {
                        outputTransportDTO.setAction(TransportActionEnum.CALL_IN_PROGRESS);
                        this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), outputTransportDTO);
                        break;
                    }
                }
                outputTransportDTO.setAction(TransportActionEnum.NO_CALL_IN_PROGRESS);
                this.messagingTemplate.convertAndSend("/topic/user/" + dto.getUserId(), outputTransportDTO);
                break;
            default:
                break;
        }
    }

    @MessageMapping("/rtc/{roomUrl}")
    public void webRtcChannel(@DestinationVariable String roomUrl, RtcTransportDTO dto) {
        RtcActionEnum action = dto.getAction();
        switch (action) {
            case INIT_ROOM -> {
                List<Integer> usersId = this.groupService.getAllUsersIdByGroupUrl(dto.getGroupUrl());
                ArrayList<Integer> userIDs = new ArrayList<>();
                userIDs.add(dto.getUserId());

                roomCacheService.putNewRoom(dto.getGroupUrl(), roomUrl, userIDs);

                OutputTransportDTO outputTransportDTO = new OutputTransportDTO();
                outputTransportDTO.setAction(TransportActionEnum.CALL_INCOMING);
                outputTransportDTO.setObject(roomUrl);
                usersId.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach((userId) -> this.messagingTemplate.convertAndSend("/topic/user/" + userId, outputTransportDTO));
            }
            case SEND_ANSWER -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                ArrayList<Integer> hostList = usersIndexedByRoomId.get(key);
                RtcTransportDTO rtcTransportDTO = new RtcTransportDTO();
                rtcTransportDTO.setUserId(dto.getUserId());
                rtcTransportDTO.setAction(RtcActionEnum.SEND_ANSWER);
                rtcTransportDTO.setAnswer(dto.getAnswer());
                hostList.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach((userId) -> this.messagingTemplate.convertAndSend("/topic/rtc/" + userId, rtcTransportDTO));
            }
            case JOIN_ROOM -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                ArrayList<Integer> hostList = usersIndexedByRoomId.get(key);
                RtcTransportDTO rtcTransportDTO = new RtcTransportDTO();
                rtcTransportDTO.setUserId(dto.getUserId());
                rtcTransportDTO.setAction(RtcActionEnum.SEND_OFFER);
                rtcTransportDTO.setOffer(dto.getOffer());
                hostList.add(dto.getUserId());
                usersIndexedByRoomId.put(roomUrl, hostList);
                hostList.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        .forEach(toUserId -> this.messagingTemplate.convertAndSend("/topic/rtc/" + toUserId, rtcTransportDTO));
            }
            case ICE_CANDIDATE -> {
                RtcTransportDTO rtcTransportDTO = new RtcTransportDTO();
                rtcTransportDTO.setUserId(dto.getUserId());
                rtcTransportDTO.setAction(RtcActionEnum.ICE_CANDIDATE);
                ArrayList<Integer> hostList = usersIndexedByRoomId.get(roomUrl);
                hostList.stream()
                        .filter((user) -> !user.equals(dto.getUserId()))
                        // TODO null ici ?
                        .forEach(toUserId -> this.messagingTemplate.convertAndSend("/topic/rtc/" + toUserId, rtcTransportDTO));
            }
            case LEAVE_ROOM -> {
                String key = roomUrl + "_" + dto.getGroupUrl();
                List<Integer> userIds = groupService.getAllUsersIdByGroupUrl(dto.getGroupUrl());
                HashMap<String, ArrayList<Integer>> hostListsIndexedByRoomUrl = roomCacheService.getRoomByKey(key);
                if (hostListsIndexedByRoomUrl.size() == 0) {
                    log.info("All users left the call, removing room from list");
                    OutputTransportDTO outputTransportDTO = new OutputTransportDTO();
                    outputTransportDTO.setAction(TransportActionEnum.END_CALL);
                    outputTransportDTO.setObject(dto.getGroupUrl());
                    usersIndexedByRoomId.remove(key);
                    userIds.forEach(userId -> this.messagingTemplate.convertAndSend("/topic/user/" + userId, outputTransportDTO));
                }
            }
            default -> log.warn("Unknown action : {}", dto.getAction());
        }
    }

    /**
     * Receive message from user and dispatch to all users subscribed to conversation
     *
     * @param userId   the int userId for mapping message to a user
     * @param groupUrl the string groupUrl for mapping message to a group
     * @param message  the payload received
     */
    public void getAndSaveMessage(int userId, String groupUrl, String message) {
        int groupId = groupService.findGroupByUrl(groupUrl);
        if (groupUserJoinService.checkIfUserIsAuthorizedInGroup(userId, groupId)) {
            return;
        }
        MessageEntity messageEntity = new MessageEntity(userId, groupId, MessageTypeEnum.TEXT.toString(), message);
        MessageEntity msg = messageService.save(messageEntity);
        List<Integer> toSend = messageService.createNotificationList(userId, groupUrl);

        // Save seen message
        seenMessageService.saveMessageNotSeen(msg, groupId);
        log.debug("Message saved");
        OutputTransportDTO dto = new OutputTransportDTO();
        dto.setAction(TransportActionEnum.NOTIFICATION_MESSAGE);
        toSend.forEach(toUserId -> {
            MessageDTO messageDTO = messageService.createNotificationMessageDTO(msg, toUserId);
            dto.setObject(messageDTO);
            messagingTemplate.convertAndSend("/topic/user/" + toUserId, dto);
        });
    }

    @MessageMapping("/message/call/{userId}/group/{groupUrl}")
    @SendTo("/topic/call/reply/{groupUrl}")
    public String wsCallMessageMapping(@DestinationVariable int userId, String req) {
        log.info("Receiving RTC data, sending back to user ...");
        return req;
    }

    @MessageMapping("/groups/create/single")
    @SendToUser("/queue/reply")
    public void wsCreateConversation(String payload) {
        Gson gson = new Gson();
        CreateGroupDTO createGroup = gson.fromJson(payload, CreateGroupDTO.class);
        Long id1 = createGroup.getId1();
        Long id2 = createGroup.getId2();
        groupService.createConversation(id1.intValue(), id2.intValue());
    }
}
