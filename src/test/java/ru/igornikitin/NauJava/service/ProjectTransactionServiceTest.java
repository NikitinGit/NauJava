package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Тесты транзакционного создания проекта с задачами.
 * Класс намеренно НЕ помечен @Transactional: иначе сервис выполнился бы внутри транзакции теста
 * и откат внутри сервиса нельзя было бы проверить.
 */
@SpringBootTest
class ProjectTransactionServiceTest {

	private final ProjectTransactionService projectTransactionService;

	private final ProjectService projectService;

	private final ProjectRepository projectRepository;

	private final TaskRepository taskRepository;

	private final UserRepository userRepository;

	private User owner;

	@Autowired
	ProjectTransactionServiceTest(ProjectTransactionService projectTransactionService, ProjectService projectService,
			ProjectRepository projectRepository, TaskRepository taskRepository, UserRepository userRepository) {
		this.projectTransactionService = projectTransactionService;
		this.projectService = projectService;
		this.projectRepository = projectRepository;
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
	}

	@BeforeEach
	void createOwner() {
		String unique = UUID.randomUUID().toString();
		owner = new User();
		owner.setLogin(unique.substring(0, 20));
		owner.setFullName("Владелец проекта");
		owner.setEmail(unique + "@test.ru");
		owner.setPassword("test");
		userRepository.save(owner);
	}

	@AfterEach
	void deleteOwner() {
		userRepository.delete(owner);
	}

	/**
	 * Положительный кейс: проект и все задачи сохранены и связаны между собой.
	 */
	@Test
	void testCreateProjectWithTasks() {
		Project project = newProject();
		Task first = newTask("Первая задача");
		Task second = newTask("Вторая задача");

		Project saved = projectTransactionService.createProjectWithTasks(project, List.of(first, second));

		try {
			Assertions.assertTrue(projectRepository.findById(saved.getId()).isPresent());
			Assertions.assertTrue(taskRepository.findById(first.getId()).isPresent());
			Assertions.assertTrue(taskRepository.findById(second.getId()).isPresent());
			Assertions.assertEquals(saved.getId(), first.getProject().getId());
			Assertions.assertEquals(saved.getId(), second.getProject().getId());
		} finally {
			projectService.deleteById(saved.getId());
		}
	}

	/**
	 * Отрицательный кейс: вторая задача без обязательного названия — транзакция откатывается,
	 * и в БД не остаётся ни проекта, ни первой задачи, хотя они были сохранены раньше ошибки.
	 */
	@Test
	void testCreateProjectWithTasksRollback() {
		Project project = newProject();
		Task valid = newTask("Корректная задача");
		Task invalid = newTask(null);

		Assertions.assertThrows(DataIntegrityViolationException.class,
				() -> projectTransactionService.createProjectWithTasks(project, List.of(valid, invalid)));

		// id были выданы при INSERT внутри транзакции, но после отката этих строк в БД нет
		Assertions.assertNotNull(project.getId());
		Assertions.assertNotNull(valid.getId());
		Assertions.assertTrue(projectRepository.findById(project.getId()).isEmpty());
		Assertions.assertTrue(taskRepository.findById(valid.getId()).isEmpty());
	}

	private Project newProject() {
		Project project = new Project();
		project.setName("Проект " + UUID.randomUUID());
		project.setDeadline(LocalDate.of(2030, 12, 31));
		project.setOwner(owner);
		return project;
	}

	private static Task newTask(String title) {
		Task task = new Task();
		task.setTitle(title);
		return task;
	}

}
