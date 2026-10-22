package ru.igornikitin.NauJava.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;

@RepositoryRestResource(path = "tasks")
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

	// служебный метод для каскадного удаления проекта; через REST не выставляется, иначе он стал бы GET /search/...
	@RestResource(exported = false)
	void deleteByProjectId(Long projectId);

}
