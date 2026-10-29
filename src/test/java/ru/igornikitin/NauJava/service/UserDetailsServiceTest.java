package ru.igornikitin.NauJava.service;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Тесты преобразования пользователя приложения в UserDetails для Spring Security.
 */
@SpringBootTest
class UserDetailsServiceTest {

	private final UserDetailsService userDetailsService;

	private final UserRepository userRepository;

	private User testUser;

	@Autowired
	UserDetailsServiceTest(UserDetailsService userDetailsService, UserRepository userRepository) {
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
	}

	@AfterEach
	void cleanUp() {
		if (testUser != null) {
			userRepository.delete(testUser);
		}
	}

	/**
	 * Логин и пароль переносятся как есть, роль превращается в право с префиксом ROLE_.
	 */
	@Test
	void testLoadUserByUsername() {
		testUser = createUser(UserRole.ADMIN);

		UserDetails details = userDetailsService.loadUserByUsername(testUser.getLogin());

		Assertions.assertEquals(testUser.getLogin(), details.getUsername());
		Assertions.assertEquals(testUser.getPassword(), details.getPassword());
		Assertions.assertEquals(List.of("ROLE_ADMIN"),
				details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
	}

	/**
	 * Для неизвестного логина бросается UsernameNotFoundException — его ожидает Spring Security.
	 */
	@Test
	void testLoadUnknownUser() {
		Assertions.assertThrows(UsernameNotFoundException.class,
				() -> userDetailsService.loadUserByUsername(UUID.randomUUID().toString()));
	}

	private User createUser(UserRole role) {
		String unique = UUID.randomUUID().toString();
		User user = new User();
		user.setLogin(unique.substring(0, 20));
		user.setFullName("Тестовый пользователь");
		user.setEmail(unique + "@test.ru");
		user.setPassword("test");
		user.setRole(role);
		return userRepository.save(user);
	}

}
