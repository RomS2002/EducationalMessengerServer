package ru.roms2002.messenger.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.MessageUserEntity;
import ru.roms2002.messenger.server.entity.MessageUserKey;

@Repository
public interface MessageUserRepository extends JpaRepository<MessageUserEntity, MessageUserKey> {

	@Query("from MessageUserEntity where message.id = :messageId and user.id = :userId")
	Optional<MessageUserEntity> findByMessageIdAndUserId(int messageId, int userId);

	@Query("from MessageUserEntity where message.id = :messageId")
	List<MessageUserEntity> findByMessageId(int messageId);
}
