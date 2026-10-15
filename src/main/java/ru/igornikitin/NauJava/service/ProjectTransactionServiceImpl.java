package ru.igornikitin.NauJava.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;

/**
 * Транзакция управляется вручную через PlatformTransactionManager (как в приложении Д методички).
 * Декларативный аналог — аннотация @Transactional на методе: Spring сам сделает commit или rollback.
 */
@Service
public class ProjectTransactionServiceImpl implements ProjectTransactionService {

	private final ProjectRepository projectRepository;

	private final TaskRepository taskRepository;

	private final PlatformTransactionManager transactionManager;

	public ProjectTransactionServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository,
			PlatformTransactionManager transactionManager) {
		this.projectRepository = projectRepository;
		this.taskRepository = taskRepository;
		this.transactionManager = transactionManager;
	}

	@Override
	public Project createProjectWithTasks(Project project, List<Task> tasks) {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			Project savedProject = projectRepository.save(project);
			for (Task task : tasks) {
				task.setProject(savedProject);
				taskRepository.save(task);
			}
			transactionManager.commit(status);
			return savedProject;
		} catch (RuntimeException ex) {
			// если упал сам commit, транзакция уже завершена и откатывать нечего
			if (!status.isCompleted()) {
				transactionManager.rollback(status);
			}
			throw ex;
		}
	}

}
