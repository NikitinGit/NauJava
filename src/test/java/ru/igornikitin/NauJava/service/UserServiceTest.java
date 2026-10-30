package ru.igornikitin.NauJava.service;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Тесты добавления и получения пользователя через UserService.
 */
@SpringBootTest
class UserServiceTest {

	private final UserService userService;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private User testUser;

	@Autowired
	UserServiceTest(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@AfterEach
	void cleanUp() {
		if (testUser != null) {
			userRepository.delete(testUser);
		}
	}

	/**
	 * Созданный пользователь находится по логину и по умолчанию получает роль USER.
	 */
	@Test
	void testCreateAndFindByLogin() {
		String login = UUID.randomUUID().toString().substring(0, 20);

		testUser = userService.createUser(login, "Тестовый пользователь", login + "@test.ru", "secret");
		User found = userService.findByLogin(login);

		Assertions.assertEquals(testUser.getId(), found.getId());
		Assertions.assertEquals(UserRole.USER, found.getRole());
	}

	/**
	 * В БД сохраняется BCrypt-хеш, а не сам пароль; введённый пароль с хешем совпадает.
	 */
	@Test
	void testPasswordIsEncoded() {
		String login = UUID.randomUUID().toString().substring(0, 20);

		testUser = userService.createUser(login, "Тестовый пользователь", login + "@test.ru", "secret");
		String stored = userService.findByLogin(login).getPassword();

		Assertions.assertNotEquals("secret", stored);
		Assertions.assertTrue(stored.startsWith("$2a$"), stored);
		Assertions.assertTrue(passwordEncoder.matches("secret", stored));
	}

	/**
	 * Второй пользователь с тем же логином не создаётся.
	 */
	@Test
	void testCreateUserWithExistingLogin() {
		String login = UUID.randomUUID().toString().substring(0, 20);
		testUser = userService.createUser(login, "Тестовый пользователь", login + "@test.ru", "secret");

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.createUser(login, "Другой пользователь", "other-" + login + "@test.ru", "secret"));
	}

	/**
	 * Поиск по несуществующему логину завершается понятной ошибкой.
	 */
	@Test
	void testFindByUnknownLogin() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.findByLogin(UUID.randomUUID().toString()));
	}

}
