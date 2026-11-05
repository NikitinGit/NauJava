package ru.igornikitin.NauJava.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;
import ru.igornikitin.NauJava.service.UserService;

/**
 * Проверка критериев практической работы 6: правила доступа, вход, выход и регистрация.
 * Запросы выполняются через MockMvc — с цепочкой фильтров Spring Security, но без запуска сервера.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

	private static final String SWAGGER = "/swagger-ui/index.html";

	private static final String API_DOCS = "/v3/api-docs";

	private static final String REST_API = "/custom/tasks/findByAssigneeLogin?login=nobody";

	private final MockMvc mockMvc;

	private final UserService userService;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final List<String> createdLogins = new ArrayList<>();

	@Autowired
	SecurityConfigTest(MockMvc mockMvc, UserService userService, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.mockMvc = mockMvc;
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@AfterEach
	void cleanUp() {
		createdLogins.forEach(login -> userRepository.findByLogin(login).ifPresent(userRepository::delete));
	}

	/**
	 * Неавторизованному пользователю ресурсы недоступны: REST API, HTML-страницы и Swagger перенаправляют на вход.
	 */
	@Test
	void testAnonymousIsRedirectedToLogin() throws Exception {
		for (String url : List.of(REST_API, "/tasks", "/custom/projects/view/list", SWAGGER)) {
			mockMvc.perform(get(url))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login"));
		}
	}

	/**
	 * Страницы входа и регистрации открыты всем.
	 */
	@Test
	void testLoginAndRegistrationArePublic() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().isOk());
		mockMvc.perform(get("/registration")).andExpect(status().isOk());
	}

	/**
	 * Обычному пользователю доступен REST API, но не swagger-ui.
	 */
	@Test
	@WithMockUser(roles = "USER")
	void testUserHasNoAccessToSwagger() throws Exception {
		mockMvc.perform(get(REST_API)).andExpect(status().isOk());
		mockMvc.perform(get(SWAGGER)).andExpect(status().isForbidden());
		mockMvc.perform(get(API_DOCS)).andExpect(status().isForbidden());
	}

	/**
	 * Администратору swagger-ui доступен.
	 */
	@Test
	@WithMockUser(roles = "ADMIN")
	void testAdminHasAccessToSwagger() throws Exception {
		mockMvc.perform(get(SWAGGER)).andExpect(status().isOk());
		mockMvc.perform(get(API_DOCS)).andExpect(status().isOk());
	}

	/**
	 * Вход через форму проверяет пароль по BCrypt-хешу из БД.
	 */
	@Test
	void testFormLogin() throws Exception {
		String login = newLogin();
		userService.createUser(login, "Тестовый пользователь", login + "@test.ru", "secret");

		mockMvc.perform(formLogin().user(login).password("secret")).andExpect(authenticated().withUsername(login));
		mockMvc.perform(formLogin().user(login).password("wrong"))
				.andExpect(unauthenticated())
				.andExpect(redirectedUrl("/login?error"));
	}

	/**
	 * Пользователь может выйти из системы.
	 */
	@Test
	@WithMockUser
	void testLogout() throws Exception {
		mockMvc.perform(post("/logout").with(csrf()))
				.andExpect(redirectedUrl("/login?logout"))
				.andExpect(unauthenticated());
	}

	/**
	 * Регистрация создаёт пользователя с ролью USER и зашифрованным паролем; поле role из запроса игнорируется.
	 */
	@Test
	void testRegistration() throws Exception {
		String login = newLogin();

		mockMvc.perform(post("/registration").with(csrf())
						.param("login", login)
						.param("fullName", "Новый пользователь")
						.param("email", login + "@test.ru")
						.param("password", "secret")
						.param("role", "ADMIN"))
				.andExpect(redirectedUrl("/login?registered"));

		User user = userRepository.findByLogin(login).orElseThrow();
		Assertions.assertEquals(UserRole.USER, user.getRole());
		Assertions.assertNotEquals("secret", user.getPassword());
		Assertions.assertTrue(passwordEncoder.matches("secret", user.getPassword()));
	}

	/**
	 * При занятом логине форма регистрации открывается снова, второй пользователь не создаётся.
	 */
	@Test
	void testRegistrationWithExistingLogin() throws Exception {
		String login = newLogin();
		userService.createUser(login, "Тестовый пользователь", login + "@test.ru", "secret");

		mockMvc.perform(post("/registration").with(csrf())
						.param("login", login)
						.param("fullName", "Другой пользователь")
						.param("email", "other-" + login + "@test.ru")
						.param("password", "secret"))
				.andExpect(status().isOk());

		Assertions.assertFalse(userRepository.existsByEmail("other-" + login + "@test.ru"));
	}

	private String newLogin() {
		String login = UUID.randomUUID().toString().substring(0, 20);
		createdLogins.add(login);
		return login;
	}

}
