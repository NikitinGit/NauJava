package ru.igornikitin.NauJava.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.criteria.TaskCriteriaRepository;

/**
 * Тесты кастомных запросов TaskRepository (Spring Data) и их аналогов на Criteria API.
 * Тесты работают с настоящей БД, поэтому проверяют только «свои» задачи (по id),
 * а созданные данные удаляют после себя.
 */
@SpringBootTest
class TaskRepositoryTest {

	private static final LocalDate FROM = LocalDate.of(2030, 1, 1);

	private static final LocalDate TO = LocalDate.of(2030, 1, 31);

	private final TaskRepository taskRepository;

	private final TaskCriteriaRepository taskCriteriaRepository;

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final List<Task> createdTasks = new ArrayList<>();

	private final List<Project> createdProjects = new ArrayList<>();

	private final List<User> createdUsers = new ArrayList<>();

	@Autowired
	TaskRepositoryTest(TaskRepository taskRepository, TaskCriteriaRepository taskCriteriaRepository,
			ProjectRepository projectRepository, UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.taskCriteriaRepository = taskCriteriaRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
	}

	@AfterEach
	void cleanUp() {
		taskRepository.deleteAll(createdTasks);
		projectRepository.deleteAll(createdProjects);
		userRepository.deleteAll(createdUsers);
	}

	/**
	 * Поиск по статусу и интервалу срока: попадают только задачи в нужном статусе
	 * со сроком внутри интервала, границы интервала включаются.
	 */
	@Test
	void testFindByStatusAndDueDateBetween() {
		Project project = createProject(createUser());
		Task inRange = createTask(project, TaskStatus.TODO, LocalDate.of(2030, 1, 15), null);
		Task onBorder = createTask(project, TaskStatus.TODO, TO, null);
		Task outOfRange = createTask(project, TaskStatus.TODO, LocalDate.of(2030, 2, 1), null);
		Task otherStatus = createTask(project, TaskStatus.DONE, LocalDate.of(2030, 1, 15), null);

		Set<Long> found = ids(taskRepository.findByStatusAndDueDateBetween(TaskStatus.TODO, FROM, TO));

		Assertions.assertTrue(found.contains(inRange.getId()));
		Assertions.assertTrue(found.contains(onBorder.getId()));
		Assertions.assertFalse(found.contains(outOfRange.getId()));
		Assertions.assertFalse(found.contains(otherStatus.getId()));
	}

	/**
	 * Тот же поиск на Criteria API возвращает те же задачи, что и Spring Data.
	 */
	@Test
	void testCriteriaFindByStatusAndDueDateBetween() {
		Project project = createProject(createUser());
		Task inRange = createTask(project, TaskStatus.TODO, LocalDate.of(2030, 1, 15), null);
		Task onBorder = createTask(project, TaskStatus.TODO, TO, null);
		Task outOfRange = createTask(project, TaskStatus.TODO, LocalDate.of(2030, 2, 1), null);
		Task otherStatus = createTask(project, TaskStatus.DONE, LocalDate.of(2030, 1, 15), null);
		Set<Long> ourTasks = Set.of(inRange.getId(), onBorder.getId(), outOfRange.getId(), otherStatus.getId());

		Set<Long> criteria = ids(taskCriteriaRepository.findByStatusAndDueDateBetween(TaskStatus.TODO, FROM, TO));
		Set<Long> springData = ids(taskRepository.findByStatusAndDueDateBetween(TaskStatus.TODO, FROM, TO));

		Assertions.assertEquals(Set.of(inRange.getId(), onBorder.getId()), only(criteria, ourTasks));
		Assertions.assertEquals(only(springData, ourTasks), only(criteria, ourTasks));
	}

	/**
	 * Поиск задач по логину исполнителя (JPQL через связанную сущность User).
	 */
	@Test
	void testFindByAssigneeLogin() {
		User owner = createUser();
		User assignee = createUser();
		Project project = createProject(owner);
		Task first = createTask(project, TaskStatus.TODO, null, assignee);
		Task second = createTask(project, TaskStatus.DONE, null, assignee);
		createTask(project, TaskStatus.TODO, null, owner);
		createTask(project, TaskStatus.TODO, null, null);

		Set<Long> found = ids(taskRepository.findByAssigneeLogin(assignee.getLogin()));

		Assertions.assertEquals(Set.of(first.getId(), second.getId()), found);
	}

	/**
	 * Тот же поиск на Criteria API возвращает те же задачи, что и Spring Data.
	 */
	@Test
	void testCriteriaFindByAssigneeLogin() {
		User owner = createUser();
		User assignee = createUser();
		Project project = createProject(owner);
		Task first = createTask(project, TaskStatus.TODO, null, assignee);
		Task second = createTask(project, TaskStatus.DONE, null, assignee);
		createTask(project, TaskStatus.TODO, null, owner);

		Set<Long> criteria = ids(taskCriteriaRepository.findByAssigneeLogin(assignee.getLogin()));

		Assertions.assertEquals(Set.of(first.getId(), second.getId()), criteria);
		Assertions.assertEquals(ids(taskRepository.findByAssigneeLogin(assignee.getLogin())), criteria);
	}

	/**
	 * Поиск по логину несуществующего пользователя возвращает пустой список, а не ошибку.
	 */
	@Test
	void testFindByUnknownAssigneeLogin() {
		String unknownLogin = UUID.randomUUID().toString();

		Assertions.assertTrue(taskRepository.findByAssigneeLogin(unknownLogin).isEmpty());
		Assertions.assertTrue(taskCriteriaRepository.findByAssigneeLogin(unknownLogin).isEmpty());
	}

	private User createUser() {
		String unique = UUID.randomUUID().toString();
		User user = new User();
		user.setLogin(unique.substring(0, 20));
		user.setFullName("Тестовый пользователь");
		user.setEmail(unique + "@test.ru");
		user.setPassword("test");
		createdUsers.add(userRepository.save(user));
		return user;
	}

	private Project createProject(User owner) {
		Project project = new Project();
		project.setName("Проект " + UUID.randomUUID());
		project.setDeadline(LocalDate.of(2030, 12, 31));
		project.setOwner(owner);
		createdProjects.add(projectRepository.save(project));
		return project;
	}

	private Task createTask(Project project, TaskStatus status, LocalDate dueDate, User assignee) {
		Task task = new Task();
		task.setTitle("Задача " + UUID.randomUUID());
		task.setStatus(status);
		task.setDueDate(dueDate);
		task.setProject(project);
		task.setAssignee(assignee);
		createdTasks.add(taskRepository.save(task));
		return task;
	}

	private static Set<Long> ids(List<Task> tasks) {
		return tasks.stream().map(Task::getId).collect(Collectors.toSet());
	}

	/** Оставляет из найденных только задачи, созданные этим тестом: в БД могут быть и чужие. */
	private static Set<Long> only(Set<Long> found, Set<Long> ours) {
		return found.stream().filter(ours::contains).collect(Collectors.toSet());
	}

}
