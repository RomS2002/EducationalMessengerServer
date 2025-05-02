package ru.roms2002.messenger.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {

	@Query("from ChatEntity where (id in (select chat.id from UserChatEntity where user = :user1)) and (id in (select chat.id from UserChatEntity where user = :user2)) and (type = 'SINGLE')")
	Optional<ChatEntity> findChatBetween(UserEntity user1, UserEntity user2);
}
