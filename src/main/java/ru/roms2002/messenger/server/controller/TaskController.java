package ru.roms2002.messenger.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ru.roms2002.messenger.server.dto.TaskDTO;
import ru.roms2002.messenger.server.service.TaskService;

@RestController
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

	@Autowired
	private TaskService taskService;

	@PostMapping("/create")
	public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDto) {
		taskDto = taskService.createTask(taskDto.getText());
		if (taskDto == null)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(taskDto, HttpStatus.OK);
	}

	@GetMapping
	List<TaskDTO> getTasks() {
		return taskService.getUserTasks().stream()
				.map(te -> new TaskDTO(te.getId(), te.getText(), te.isCompleted())).toList();
	}

	@PostMapping("/change-status")
	public ResponseEntity<Void> changeStatus(@RequestBody TaskDTO taskDto) {
		if (!taskService.changeTaskStatus(taskDto.getId(), taskDto.isCompleted()))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/delete/{taskId}")
	public ResponseEntity<Void> changeStatus(@PathVariable Integer taskId) {
		if (!taskService.deleteById(taskId))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
