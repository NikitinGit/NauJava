package ru.igornikitin.NauJava.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.igornikitin.NauJava.dto.TaskDto;
import ru.igornikitin.NauJava.entity.TaskStatus;
import ru.igornikitin.NauJava.repository.TaskRepository;

/**
 * REST-контроллер для кастомных запросов TaskRepository из практики 4.
 * Пути сделаны по примеру из методички (/findByName) — это RPC-стиль: действие записано в URL.
 * В RESTful-стиле были бы фильтры коллекции: GET /custom/tasks?status=…&from=…&to=… и GET /custom/tasks?assignee=…
 * Вопрос о переходе на RESTful-стиль задан куратору.
 */
@RestController
@RequestMapping("/custom/tasks")
public class TaskController {

	private final TaskRepository taskRepository;

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	/**
	 * Пример: /custom/tasks/findByStatusAndDueDateBetween?status=IN_PROGRESS&from=2030-01-01&to=2030-01-31
	 */
	@GetMapping("/findByStatusAndDueDateBetween")
	public List<TaskDto> findByStatusAndDueDateBetween(@RequestParam TaskStatus status,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return taskRepository.findByStatusAndDueDateBetween(status, from, to).stream().map(TaskDto::from).toList();
	}

	/**
	 * Пример: /custom/tasks/findByAssigneeLogin?login=ivanov
	 */
	@GetMapping("/findByAssigneeLogin")
	public List<TaskDto> findByAssigneeLogin(@RequestParam String login) {
		return taskRepository.findByAssigneeLogin(login).stream().map(TaskDto::from).toList();
	}

}
