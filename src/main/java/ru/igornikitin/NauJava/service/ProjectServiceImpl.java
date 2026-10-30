package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import ru.igornikitin.NauJava.config.AppConfig;
import ru.igornikitin.NauJava.entity.MemberRole;
import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.ProjectMember;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.CommentRepository;
import ru.igornikitin.NauJava.repository.ProjectMemberRepository;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.TaskRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepository;

	private final ProjectMemberRepository projectMemberRepository;

	private final TaskRepository taskRepository;

	private final CommentRepository commentRepository;

	private final UserService userService;

	private final AppConfig appConfig;

	public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
			TaskRepository taskRepository, CommentRepository commentRepository, UserService userService,
			AppConfig appConfig) {
		this.projectRepository = projectRepository;
		this.projectMemberRepository = projectMemberRepository;
		this.taskRepository = taskRepository;
		this.commentRepository = commentRepository;
		this.userService = userService;
		this.appConfig = appConfig;
	}

	@PostConstruct
	public void printAppInfo() {
		System.out.println(appConfig.getAppName() + " v" + appConfig.getAppVersion());
	}

	@Override
	@Transactional
	public Project createProject(String ownerLogin, String name, String description, LocalDate deadline) {
		requireNotBlank(name, "название проекта");
		if (deadline == null) {
			throw new IllegalArgumentException("срок проекта обязателен");
		}
		User owner = userService.findByLogin(ownerLogin);
		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		project.setDeadline(deadline);
		project.setOwner(owner);
		projectRepository.save(project);
		addMember(project, owner, MemberRole.OWNER);
		return project;
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
	@Transactional
	public void deleteById(Long id) {
		findById(id);
		// сначала зависимые записи, иначе БД не даст удалить проект из-за внешних ключей
		commentRepository.deleteByTaskProjectId(id);
		taskRepository.deleteByProjectId(id);
		projectMemberRepository.deleteByProjectId(id);
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
	public void addMember(Long projectId, String userLogin) {
		Project project = findById(projectId);
		User user = userService.findByLogin(userLogin);
		if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
			throw new IllegalArgumentException("Участник " + userLogin + " уже есть в проекте " + projectId);
		}
		addMember(project, user, MemberRole.MEMBER);
	}

	private void addMember(Project project, User user, MemberRole role) {
		ProjectMember member = new ProjectMember();
		member.setProject(project);
		member.setUser(user);
		member.setRole(role);
		projectMemberRepository.save(member);
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " не может быть пустым");
		}
	}

}
