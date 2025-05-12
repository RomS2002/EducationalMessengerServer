package ru.roms2002.messenger.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {

	@Query("from ChatEntity where (id in (select chat.id from UserChatEntity where user = :user1)) and (id in (select chat.id from UserChatEntity where user = :user2)) and (type = 'SINGLE')")
	Optional<ChatEntity> findChatBetween(UserEntity user1, UserEntity user2);

	@Query("from ChatEntity where type = ChatTypeEnum.STUDGROUP and name = :groupName")
	Optional<ChatEntity> findByStudgroupName(String groupName);

	@Query("from ChatEntity where type = ChatTypeEnum.DEPARTMENT and name like %:department%")
	Optional<ChatEntity> findByDepartment(@Param("department") String department);

	@Query("from ChatEntity where type = ChatTypeEnum.SELF and id in (select chat.id from UserChatEntity where user.id = :userId)")
	ChatEntity findSelfChat(int userId);

	@Query("from ChatEntity where type <> ChatTypeEnum.SELF and id in (select chat.id from UserChatEntity where user.id = :userId)")
	List<ChatEntity> findByUserId(int userId);
}