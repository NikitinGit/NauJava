package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
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
		projectRepository.create(project);
		return project;
	}

	@Override
	public Project findById(Long id) {
		Project project = projectRepository.read(id);
		if (project == null) {
			throw new IllegalArgumentException("Проект с id " + id + " не найден");
		}
		return project;
	}

	@Override
	public List<Project> findAll() {
		return projectRepository.readAll();
	}

	@Override
	public void deleteById(Long id) {
		findById(id);
		projectRepository.delete(id);
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
		projectRepository.update(project);
	}

	@Override
	public void addMember(Long projectId, String member) {
		requireNotBlank(member, "имя участника");
		Project project = findById(projectId);
		if (!project.getMembers().add(member)) {
			throw new IllegalArgumentException("Участник " + member + " уже есть в проекте " + projectId);
		}
		projectRepository.update(project);
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " не может быть пустым");
		}
	}

}
