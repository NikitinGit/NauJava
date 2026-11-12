package ru.igornikitin.NauJava.repository.criteria;

import java.time.LocalDate;
import java.util.List;

import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;

/** Аналоги запросов TaskRepository, написанные на Criteria API. */
public interface TaskCriteriaRepository {

	/**
	 * Находит задачи в заданном статусе со сроком в интервале [from, to] включительно.
	 * @param status статус задачи
	 * @param from начало интервала срока
	 * @param to конец интервала срока
	 */
	List<Task> findByStatusAndDueDateBetween(TaskStatus status, LocalDate from, LocalDate to);

	/**
	 * Находит задачи, назначенные пользователю, по его логину — через связанную сущность User.
	 * @param login логин исполнителя
	 */
	List<Task> findByAssigneeLogin(String login);

}
