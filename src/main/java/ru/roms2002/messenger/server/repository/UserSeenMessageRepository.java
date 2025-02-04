package ru.roms2002.messenger.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.MessageUserEntity;
import ru.roms2002.messenger.server.entity.MessageUserKey;

@Repository
public interface UserSeenMessageRepository extends JpaRepository<MessageUserEntity, MessageUserKey> {

    MessageUserEntity findAllByMessageIdAndUserId(int messageId, int userId);

}
