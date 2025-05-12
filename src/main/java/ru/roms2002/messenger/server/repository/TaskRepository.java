package ru.roms2002.messenger.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.roms2002.messenger.server.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

	@Query("from TaskEntity where user.id = :userId order by completed, id desc")
	List<TaskEntity> findByUserId(int userId);
}
