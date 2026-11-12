package ru.igornikitin.NauJava.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.repository.ProjectRepository;

/**
 * Web-контроллер: отдаёт HTML-страницы (шаблоны Thymeleaf из src/main/resources/templates), а не JSON.
 */
@Controller
@RequestMapping("/custom/projects/view")
public class ProjectViewController {

	private final ProjectRepository projectRepository;

	public ProjectViewController(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@GetMapping("/list")
	public String projectListView(Model model, Principal principal) {
		Iterable<Project> projects = projectRepository.findAll();
		model.addAttribute("projects", projects);
		// Principal — вошедший пользователь, его подставляет Spring Security
		model.addAttribute("username", principal.getName());
		return "projectList";
	}

}
