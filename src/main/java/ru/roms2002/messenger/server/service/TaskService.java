package ru.roms2002.messenger.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.dto.TaskDTO;
import ru.roms2002.messenger.server.entity.TaskEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.TaskRepository;

@Service
public class TaskService {

	@Autowired
	private UserService userService;

	@Autowired
	private TaskRepository taskRepository;

	public TaskDTO createTask(String text) {
		UserEntity user = userService.getCurrentUser();
		if (text == null || text.isBlank())
			return null;

		TaskEntity task = new TaskEntity();
		task.setCompleted(false);
		task.setText(text);
		task.setUser(user);
		try {
			task = save(task);
			return new TaskDTO(task.getId(), task.getText(), task.isCompleted());
		} catch (Exception e) {
			return null;
		}

	}

	public List<TaskEntity> getUserTasks() {
		UserEntity user = userService.getCurrentUser();
		return taskRepository.findByUserId(user.getId());
	}

	public boolean changeTaskStatus(int taskId) {
		List<TaskEntity> userTasks = getUserTasks();
		List<TaskEntity> tmp = userTasks.stream().filter(t -> t.getId() == taskId).toList();
		if (tmp.size() == 0)
			return false;
		TaskEntity task = tmp.get(0);
		task.setCompleted(!task.isCompleted());
		try {
			task = save(task);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@CachePut("tasks")
	public TaskEntity save(TaskEntity task) {
		return taskRepository.save(task);
	}

	@CacheEvict("tasks")
	public boolean deleteById(int taskId) {
		try {
			taskRepository.deleteById(taskId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
