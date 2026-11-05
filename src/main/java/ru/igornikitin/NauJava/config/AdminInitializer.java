package ru.igornikitin.NauJava.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;
import ru.igornikitin.NauJava.service.UserService;

/**
 * Создаёт администратора при старте приложения, если его ещё нет: через регистрацию появляются только USER.
 * Пароль не хранится в репозитории — берётся из переменной окружения ADMIN_PASSWORD;
 * если она не задана, администратор не создаётся.
 */
@Component
// раньше консольного CommandLineRunner: тот блокирует запуск, пока ждёт ввода
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

	private final UserService userService;

	private final UserRepository userRepository;

	@Value("${app.admin.login}")
	private String login;

	@Value("${app.admin.full-name}")
	private String fullName;

	@Value("${app.admin.email}")
	private String email;

	@Value("${app.admin.password}")
	private String password;

	public AdminInitializer(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (userRepository.existsByLogin(login)) {
			return;
		}
		if (password.isBlank()) {
			log.warn("Администратор '{}' не создан: не задана переменная окружения ADMIN_PASSWORD", login);
			return;
		}
		userService.createUser(login, fullName, email, password, UserRole.ADMIN);
		log.info("Создан администратор '{}'", login);
	}

}
