package ru.roms2002.messenger.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

	List<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByAdminpanelId(Integer id);

	@Query("from UserEntity where id in (select user.id from UserChatEntity where chat.id = :chatId)")
	List<UserEntity> getUsersInChat(int chatId);
}
