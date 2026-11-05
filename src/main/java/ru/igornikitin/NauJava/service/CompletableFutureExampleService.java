package ru.igornikitin.NauJava.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.stereotype.Service;

import ru.igornikitin.NauJava.dto.CombineExampleDto;
import ru.igornikitin.NauJava.dto.CombineErrorExampleDto;
import ru.igornikitin.NauJava.dto.ComposeExampleDto;
import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/** Небольшие изолированные примеры thenCombine и thenCompose на данных ProjectManager. */
@Service
public class CompletableFutureExampleService {

	private final UserRepository userRepository;

	private final ProjectRepository projectRepository;

	private final TaskRepository taskRepository;

	public CompletableFutureExampleService(UserRepository userRepository, ProjectRepository projectRepository,
			TaskRepository taskRepository) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.taskRepository = taskRepository;
	}

	/**
	 * thenCombine объединяет результаты двух независимых асинхронных операций.
	 * Подсчёт пользователей и получение проектов могут выполняться параллельно.
	 */
	public CompletableFuture<CombineExampleDto> thenCombineExample() {
		CompletableFuture<Long> userCount = CompletableFuture.supplyAsync(userRepository::count);
		CompletableFuture<List<String>> projectNames = CompletableFuture.supplyAsync(() ->
				projectRepository.findAllWithOwner().stream().map(Project::getName).toList());

		return userCount.thenCombine(projectNames, CombineExampleDto::new);
	}

	/**
	 * Демонстрирует ошибку thenCombine: первая задача падает сразу, но вторая не отменяется.
	 * handle превращает ожидаемую ошибку в DTO, чтобы пример можно было посмотреть через HTTP.
	 */
	public CompletableFuture<CombineErrorExampleDto> thenCombineWithErrorExample() {
		long startedAt = System.currentTimeMillis();
		CompletableFuture<Integer> failedTask = CompletableFuture.supplyAsync(() -> {
			System.out.println("task1 begin");
			//return 125;
			throw new IllegalStateException("Первая задача завершилась с ошибкой");
		});
		CompletableFuture<String> slowTask = CompletableFuture.supplyAsync(() -> {
			System.out.println("task2 begin");
			sleep(5000);
			return "Вторая задача завершилась нормально";
		});

		System.out.println("failedTask.thenCombine");
		return failedTask.thenCombine(slowTask, (number, text) -> (text + ": " + number))
				.handle((result, exception) -> {
					System.out.println("task1 and task2 is over");

					long elapsed = System.currentTimeMillis() - startedAt;

					if (exception == null) {
						// Обе задачи выполнились успешно
						return new CombineErrorExampleDto(
								"Нет ошибки",
								"Обе задачи завершились успешно: " + result,
								slowTask.isDone(),
								elapsed
						);
					}

					// Хотя бы одна задача завершилась с ошибкой
					Throwable cause = exception instanceof CompletionException && exception.getCause() != null
							? exception.getCause()
							: exception;

					return new CombineErrorExampleDto(
							cause.getClass().getSimpleName(),
							cause.getMessage(),
							slowTask.isDone(),
							elapsed
					);
				});
	}

	/**
	 * thenCompose запускает вторую асинхронную операцию после первой, когда для неё нужен результат первой.
	 * Сначала находится проект, затем по его id запрашиваются задачи этого проекта.
	 */
	public CompletableFuture<ComposeExampleDto> thenComposeExample(Long projectId) {
		return findProjectAsync(projectId).thenCompose(project ->
				findTasksByProjectIdAsync(project.getId()).thenApply(tasks -> new ComposeExampleDto(
						project.getId(), project.getName(), tasks.stream().map(Task::getTitle).toList())));
	}

	private CompletableFuture<Project> findProjectAsync(Long projectId) {
		return CompletableFuture.supplyAsync(() -> projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Проект с id " + projectId + " не найден")));
	}

	private CompletableFuture<List<Task>> findTasksByProjectIdAsync(Long projectId) {
		return CompletableFuture.supplyAsync(() -> taskRepository.findByProjectId(projectId));
	}

	private void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Вторая задача была прервана", ex);
		}
	}

}
