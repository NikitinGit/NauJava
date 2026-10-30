package ru.igornikitin.NauJava.dto;

import java.time.LocalDate;

import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskPriority;
import ru.igornikitin.NauJava.entity.TaskStatus;

/**
 * Задача в ответе REST-контроллера.
 * Сущность Task напрямую не отдаётся: Jackson пошёл бы по связям Task → Project → tasks → Task
 * и ушёл бы в бесконечную рекурсию, поэтому связанные сущности заменены их id и логином.
 */
public record TaskDto(Long id, String title, String description, TaskStatus status, TaskPriority priority,
		LocalDate dueDate, Long projectId, String assigneeLogin) {

	public static TaskDto from(Task task) {
		return new TaskDto(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
				task.getPriority(), task.getDueDate(), task.getProject().getId(),
				task.getAssignee() == null ? null : task.getAssignee().getLogin());
	}

}
