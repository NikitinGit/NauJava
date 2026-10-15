package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import ru.igornikitin.NauJava.config.AppConfig;
import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.repository.ProjectRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepository;

	private final AppConfig appConfig;

	public ProjectServiceImpl(ProjectRepository projectRepository, AppConfig appConfig) {
		this.projectRepository = projectRepository;
		this.appConfig = appConfig;
	}

	@PostConstruct
	public void printAppInfo() {
		System.out.println(appConfig.getAppName() + " v" + appConfig.getAppVersion());
	}

	@Override
	public Project createProject(String name, String description, LocalDate deadline) {
		requireNotBlank(name, "название проекта");
		if (deadline == null) {
			throw new IllegalArgumentException("срок проекта обязателен");
		}
		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		project.setDeadline(deadline);
		return projectRepository.save(project);
	}

	@Override
	public Project findById(Long id) {
		return projectRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Проект с id " + id + " не найден"));
	}

	@Override
	public List<Project> findAll() {
		// CrudRepository.findAll() возвращает Iterable, а не List
		List<Project> projects = new ArrayList<>();
		projectRepository.findAll().forEach(projects::add);
		return projects;
	}

	@Override
	public void deleteById(Long id) {
		findById(id);
		projectRepository.deleteById(id);
	}

	@Override
	public void updateProject(Long id, String name, String description, LocalDate deadline) {
		Project project = findById(id);
		if (name != null) {
			requireNotBlank(name, "название проекта");
			project.setName(name);
		}
		if (description != null) {
			project.setDescription(description);
		}
		if (deadline != null) {
			project.setDeadline(deadline);
		}
		projectRepository.save(project);
	}

	@Override
	public void addMember(Long projectId, String member) {
		// TODO временная заглушка: участники стали сущностью ProjectMember, метод переписывается на шаге 7
		throw new UnsupportedOperationException("добавление участников временно недоступно");
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " не может быть пустым");
		}
	}

}
