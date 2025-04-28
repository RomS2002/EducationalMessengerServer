package ru.roms2002.messenger.server.mapper;

import org.springframework.stereotype.Service;

@Service
public class GroupCallMapper {

//    @Autowired
//    private RoomCacheService roomCacheService;
//
//    public GroupCallDTO toGroupCall(ChatEntity group) {
//        List<String> keys = roomCacheService.getAllKeys();
//        GroupCallDTO groupCallDTO = new GroupCallDTO();
//        Optional<String> actualRoomKey =
//                keys.stream().filter((key) -> {
//                    String[] roomKey = key.split("_");
//                    return group.getUrl().equals(roomKey[0]);
//                }).findFirst();
//        if (actualRoomKey.isPresent()) {
//            groupCallDTO.setAnyCallActive(true);
//            groupCallDTO.setActiveCallUrl(actualRoomKey.get().split("_")[1]);
//        } else {
//            groupCallDTO.setAnyCallActive(false);
//        }
//        return groupCallDTO;
//    }
}
