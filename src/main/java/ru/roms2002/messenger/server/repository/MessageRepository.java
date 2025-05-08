package ru.roms2002.messenger.server.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import ru.roms2002.messenger.server.entity.MessageEntity;

@Repository
public interface MessageRepository extends PagingAndSortingRepository<MessageEntity, Integer>,
		JpaRepository<MessageEntity, Integer> {

	@Query("from MessageEntity where chat.id = :id order by createdAt desc, id desc")
	List<MessageEntity> findByChatId(int id, Pageable pageable);

	@Transactional
	@Modifying
	@Query("delete from MessageEntity where chat.id = :chatId and user.id = :userId")
	void deleteAllMessagesFromUserInChat(int userId, int chatId);
}
