package ru.igornikitin.NauJava.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ru.igornikitin.NauJava.dto.CombineExampleDto;
import ru.igornikitin.NauJava.dto.CombineErrorExampleDto;
import ru.igornikitin.NauJava.dto.ComposeExampleDto;
import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/** Unit-тесты учебных примеров без подключения к PostgreSQL. */
class CompletableFutureExampleServiceTest {

	@Test
	void thenCombineWaitsForIndependentResults() {
		UserRepository users = mock(UserRepository.class);
		when(users.count()).thenReturn(2L);
		ProjectRepository projects = mock(ProjectRepository.class);
		when(projects.findAllWithOwner()).thenReturn(List.of(project(10L, "Первый проект")));
		CompletableFutureExampleService service = new CompletableFutureExampleService(users, projects,
				mock(TaskRepository.class));

		CombineExampleDto result = service.thenCombineExample().join();

		Assertions.assertEquals(2L, result.userCount());
		Assertions.assertEquals(List.of("Первый проект"), result.projectNames());
	}

	@Test
	void thenComposeStartsDependentFutureAfterProjectLookup() {
		ProjectRepository projects = mock(ProjectRepository.class);
		Project project = project(10L, "Первый проект");
		when(projects.findById(10L)).thenReturn(Optional.of(project));
		TaskRepository tasks = mock(TaskRepository.class);
		when(tasks.findByProjectId(10L)).thenReturn(List.of(task("Первая задача"), task("Вторая задача")));
		CompletableFutureExampleService service = new CompletableFutureExampleService(mock(UserRepository.class), projects,
				tasks);

		ComposeExampleDto result = service.thenComposeExample(10L).join();

		Assertions.assertEquals(10L, result.projectId());
		Assertions.assertEquals("Первый проект", result.projectName());
		Assertions.assertEquals(List.of("Первая задача", "Вторая задача"), result.taskTitles());
	}

	@Test
	void thenCombineWithErrorWaitsForSecondTask() {
		CompletableFutureExampleService service = new CompletableFutureExampleService(mock(UserRepository.class),
				mock(ProjectRepository.class), mock(TaskRepository.class));

		CombineErrorExampleDto result = service.thenCombineWithErrorExample().join();

		Assertions.assertEquals("IllegalStateException", result.exceptionType());
		Assertions.assertTrue(result.secondTaskFinished());
		Assertions.assertTrue(result.elapsedMillis() >= 450, result.elapsedMillis() + " ms");
	}

	private static Project project(Long id, String name) {
		Project project = new Project();
		project.setId(id);
		project.setName(name);
		return project;
	}

	private static Task task(String title) {
		Task task = new Task();
		task.setTitle(title);
		return task;
	}

}
