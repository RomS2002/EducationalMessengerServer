package ru.roms2002.messenger.server.service;

import org.springframework.stereotype.Service;

@Service
public class GroupUserJoinService {

//    private static final Logger log = LoggerFactory.getLogger(WsFileController.class);
//
//    @Autowired
//    private GroupUserJoinRepository groupUserJoinRepository;
//
//    @Autowired
//    private MessageService messageService;
//
//    public UserChat save(UserChat groupUser) {
//        return groupUserJoinRepository.save(groupUser);
//    }
//
//    public void saveAll(List<UserChat> groups) {
//        try {
//            groupUserJoinRepository.saveAll(groups);
//        } catch (Exception e) {
//            log.error("Cannot save user for conversation : {}", e.getMessage());
//        }
//    }
//
//    public Optional<UserChat> findById(ChatRoleKey id) {
//        return groupUserJoinRepository.findById(id);
//    }
//
//    public List<UserChat> findAll() {
//        return groupUserJoinRepository.findAll();
//    }
//
//    public List<UserChat> findAllByGroupId(int groupId) {
//        return groupUserJoinRepository.getAllByGroupId(groupId);
//    }
//
//    public boolean checkIfUserIsAuthorizedInGroup(int userId, int groupId) {
//        List<Integer> ids = groupUserJoinRepository.getUsersIdInGroup(groupId);
//        return ids.stream().noneMatch(id -> id == userId);
//    }
//
//
//    public UserChat grantUserAdminInConversation(int userId, int groupId) {
//        return executeActionOnGroupUser(userId, groupId, 1);
//    }
//
//    public void removeUserAdminFromConversation(int userIdToDelete, int groupId) {
//        executeActionOnGroupUser(userIdToDelete, groupId, 0);
//    }
//
//    private UserChat executeActionOnGroupUser(int userId, int groupId, int role) {
//        ChatRoleKey groupRoleKey = new ChatRoleKey(groupId, userId);
//        Optional<UserChat> optionalGroupUserToDelete = groupUserJoinRepository.findById(groupRoleKey);
//        if (optionalGroupUserToDelete.isPresent()) {
//            UserChat groupUser = optionalGroupUserToDelete.get();
//            groupUser.setRole(role);
//            return groupUserJoinRepository.save(groupUser);
//        }
//        return null;
//    }
//
//    public void removeUserFromConversation(int userIdToDelete, int groupId) {
//        ChatRoleKey groupRoleKey = new ChatRoleKey(groupId, userIdToDelete);
//        try {
//            Optional<UserChat> optionalGroupUserToDelete = groupUserJoinRepository.findById(groupRoleKey);
//            optionalGroupUserToDelete.ifPresent(groupUser -> groupUserJoinRepository.delete(groupUser));
//            List<Integer> usersId = groupUserJoinRepository.getUsersIdInGroup(groupId);
//            if (usersId.isEmpty()) {
//                log.info("All users have left the group [groupId => {}]. Deleting messages...", groupId);
//                messageService.deleteAllMessagesByGroupId(groupId);
//                log.info("All messages have been successfully deleted");
//            }
//        } catch (Exception exception) {
//            log.error("Error occurred during user removal : {}", exception.getMessage());
//        }
//    }
}
