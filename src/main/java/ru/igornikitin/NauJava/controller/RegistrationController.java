package ru.igornikitin.NauJava.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import ru.igornikitin.NauJava.dto.RegistrationForm;
import ru.igornikitin.NauJava.service.UserService;

/**
 * Регистрация новых пользователей (роль USER) через HTML-форму.
 */
@Controller
public class RegistrationController {

	private final UserService userService;

	public RegistrationController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/registration")
	public String registration(Model model) {
		model.addAttribute("form", RegistrationForm.empty());
		return "registration";
	}

	@PostMapping("/registration")
	public String addUser(RegistrationForm form, Model model) {
		try {
			userService.createUser(form.login(), form.fullName(), form.email(), form.password());
			return "redirect:/login?registered";
		} catch (IllegalArgumentException ex) {
			// сообщение из сервиса: логин или email заняты, не заполнено поле
			model.addAttribute("message", ex.getMessage());
			// введённые значения возвращаются в форму, чтобы не набирать их заново (кроме пароля)
			model.addAttribute("form", form);
			return "registration";
		}
	}

}
