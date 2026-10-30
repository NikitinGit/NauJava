package ru.igornikitin.NauJava.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Своя страница входа. Сам вход (POST /login) и выход (POST /logout) обрабатывает Spring Security.
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

}
