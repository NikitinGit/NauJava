package ru.igornikitin.NauJava.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * API-тесты TaskController через настоящий HTTP-сервер и RestAssured.
 * Security отключается только в контексте этого теста, чтобы проверять контракт контроллера.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "app.console.enabled=false", "app.security.enabled=false" })
class TaskControllerRestAssuredTest {

	private static final String TASK_TITLE = "Задача для API-теста";

	@LocalServerPort
	private int port;

	private final TaskRepository taskRepository;

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private Task createdTask;

	private Project createdProject;

	private User createdOwner;

	private User createdAssignee;

	@Autowired
	TaskControllerRestAssuredTest(TaskRepository taskRepository, ProjectRepository projectRepository,
			UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
	}

	@BeforeEach
	void setUpRestAssured() {
		RestAssured.port = port;
	}

	@AfterEach
	void cleanUp() {
		if (createdTask != null) {
			taskRepository.delete(createdTask);
		}
		if (createdProject != null) {
			projectRepository.delete(createdProject);
		}
		if (createdAssignee != null) {
			userRepository.delete(createdAssignee);
		}
		if (createdOwner != null) {
			userRepository.delete(createdOwner);
		}
		RestAssured.reset();
	}

	@Test
	@DisplayName("GET /custom/tasks/findByAssigneeLogin возвращает задачу назначенного пользователя")
	void returnsTasksForAssigneeLogin() {
		createdOwner = createUser();
		createdAssignee = createUser();
		createdProject = createProject(createdOwner);
		createdTask = createTask(createdProject, createdAssignee);

		given()
				.queryParam("login", createdAssignee.getLogin())
				.when()
				.get("/custom/tasks/findByAssigneeLogin")
				.then()
				.statusCode(200)
				.contentType(ContentType.JSON)
				.body("id", contains(createdTask.getId().intValue()))
				.body("title", contains(TASK_TITLE))
				.body("assigneeLogin", contains(createdAssignee.getLogin()));
	}

	@Test
	@DisplayName("GET /custom/tasks/findByAssigneeLogin возвращает пустой список для неизвестного логина")
	void returnsEmptyListForUnknownAssigneeLogin() {
		given()
				.queryParam("login", "unknown-" + UUID.randomUUID())
				.when()
				.get("/custom/tasks/findByAssigneeLogin")
				.then()
				.statusCode(200)
				.contentType(ContentType.JSON)
				.body("$", empty());
	}

	@Test
	@DisplayName("GET /custom/tasks/findByAssigneeLogin возвращает 400 без обязательного параметра login")
	void returnsBadRequestWithoutLoginParameter() {
		given()
				.when()
				.get("/custom/tasks/findByAssigneeLogin")
				.then()
				.statusCode(400);
	}

	private User createUser() {
		String unique = UUID.randomUUID().toString();
		User user = new User();
		user.setLogin(unique.substring(0, 20));
		user.setFullName("Тестовый пользователь");
		user.setEmail(unique + "@test.ru");
		user.setPassword("test");
		return userRepository.save(user);
	}

	private Project createProject(User owner) {
		Project project = new Project();
		project.setName("Проект для API-теста " + UUID.randomUUID());
		project.setDeadline(LocalDate.of(2030, 12, 31));
		project.setOwner(owner);
		return projectRepository.save(project);
	}

	private Task createTask(Project project, User assignee) {
		Task task = new Task();
		task.setTitle(TASK_TITLE);
		task.setStatus(TaskStatus.TODO);
		task.setProject(project);
		task.setAssignee(assignee);
		return taskRepository.save(task);
	}

}
