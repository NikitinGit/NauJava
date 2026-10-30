package ru.igornikitin.NauJava.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;

public interface TaskRepository extends CrudRepository<Task, Long> {

	/**
	 * Находит задачи в заданном статусе со сроком в интервале [from, to] включительно.
	 * Запрос генерируется Spring Data по имени метода (ключевые слова And, Between).
	 * @param status статус задачи
	 * @param from начало интервала срока
	 * @param to конец интервала срока
	 */
	List<Task> findByStatusAndDueDateBetween(TaskStatus status, LocalDate from, LocalDate to);

	/**
	 * Находит задачи, назначенные пользователю, по его логину — через связанную сущность User.
	 * @param login логин исполнителя
	 */
	@Query("SELECT t FROM Task t WHERE t.assignee.login = :login")
	List<Task> findByAssigneeLogin(String login);

	void deleteByProjectId(Long projectId);

}
