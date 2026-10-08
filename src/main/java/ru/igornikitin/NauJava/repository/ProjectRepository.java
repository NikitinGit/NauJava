package ru.igornikitin.NauJava.repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import ru.igornikitin.NauJava.entity.Project;

@Component
public class ProjectRepository implements CrudRepository<Project, Long> {

	private final List<Project> projectContainer;

	/** Имитация автоинкремента БД: номера удалённых проектов повторно не выдаются. */
	private final AtomicLong idSequence = new AtomicLong();

	public ProjectRepository(List<Project> projectContainer) {
		this.projectContainer = projectContainer;
	}

	@Override
	public void create(Project project) {
		project.setId(idSequence.incrementAndGet());
		projectContainer.add(project);
	}

	@Override
	public Project read(Long id) {
		return projectContainer.stream()
				.filter(project -> Objects.equals(project.getId(), id))
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<Project> readAll() {
		return List.copyOf(projectContainer);
	}

	@Override
	public void update(Project project) {
		projectContainer.replaceAll(existing ->
				Objects.equals(existing.getId(), project.getId()) ? project : existing);
	}

	@Override
	public void delete(Long id) {
		projectContainer.removeIf(project -> Objects.equals(project.getId(), id));
	}

}
