package ru.igornikitin.NauJava.service;

import java.util.List;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Task;

/** Транзакционные операции над проектом и задачами: задача не может существовать без проекта. */
public interface ProjectTransactionService {

	/**
	 * Создаёт проект вместе с задачами в одной транзакции:
	 * если не удалось сохранить хотя бы одну задачу, не сохраняется ни проект, ни задачи.
	 * @param project новый проект (владелец обязателен)
	 * @param tasks задачи проекта; ссылка на проект проставляется автоматически
	 * @return сохранённый проект
	 */
	Project createProjectWithTasks(Project project, List<Task> tasks);

}
