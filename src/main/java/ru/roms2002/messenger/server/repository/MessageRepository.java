package ru.roms2002.messenger.server.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.MessageEntity;

@Repository
public interface MessageRepository extends PagingAndSortingRepository<MessageEntity, Integer>,
		JpaRepository<MessageEntity, Integer> {

	@Query("from MessageEntity where chat.id = :id order by createdAt desc")
	List<MessageEntity> findByChatId(int id, Pageable pageable);

//	@Query(value = "SELECT * FROM (SELECT * FROM message m WHERE m.msg_group_id=:id and ((:offset > 0 and m.id < :offset) or (:offset <= 0)) ORDER BY m.id DESC LIMIT 20)t order by id", nativeQuery = true)
//	List<MessageEntity> findByGroupIdAndOffset(@Param(value = "id") int id,
//			@Param(value = "offset") int offset);
//
//	@Query(value = "SELECT * FROM (SELECT * FROM message m WHERE m.msg_group_id=:id ORDER BY m.id DESC LIMIT 20)t order by id", nativeQuery = true)
//	List<MessageEntity> findLastMessagesByGroupId(@Param(value = "id") int id);
//
//	@Query(value = "SELECT * FROM message m1 INNER JOIN (SELECT MAX(m.id) as mId FROM message m GROUP BY m.msg_group_id) temp ON temp.mId = m1.id WHERE msg_group_id = :idOfGroup", nativeQuery = true)
//	MessageEntity findLastMessageByGroupId(@Param(value = "idOfGroup") int groupId);
//
//	@Query(value = "SELECT m1.id FROM message m1 INNER JOIN (SELECT MAX(m.id) as id FROM message m GROUP BY m.msg_group_id) temp ON temp.id = m1.id WHERE msg_group_id = :idOfGroup", nativeQuery = true)
//	int findLastMessageIdByGroupId(@Param(value = "idOfGroup") int groupId);
//
//	@Modifying
//	@Query(value = "DELETE m, mu FROM message m JOIN message_user mu ON m.id = mu.message_id WHERE m.msg_group_id = :groupId", nativeQuery = true)
//	void deleteMessagesDataByGroupId(@Param(value = "groupId") int groupId);
}
