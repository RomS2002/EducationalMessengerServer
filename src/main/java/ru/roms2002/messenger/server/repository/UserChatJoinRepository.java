package ru.roms2002.messenger.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.UserChatKey;
import ru.roms2002.messenger.server.entity.UserChatEntity;

import java.util.List;

@Repository
public interface UserChatJoinRepository extends JpaRepository<UserChatEntity, UserChatKey> {

    @Query(value = "SELECT * FROM group_user WHERE group_id=:groupId", nativeQuery = true)
    List<UserChatEntity> getAllByGroupId(@Param("groupId") int groupId);

    @Query(value = "SELECT g.user_id FROM group_user g WHERE g.group_id = :groupId", nativeQuery = true)
    List<Integer> getUsersIdInGroup(@Param("groupId") int groupId);
}
