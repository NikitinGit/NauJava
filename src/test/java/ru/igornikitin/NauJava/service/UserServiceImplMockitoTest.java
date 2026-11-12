package ru.igornikitin.NauJava.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Unit-тесты UserServiceImpl: зависимости подменены Mockito, Spring и PostgreSQL не запускаются.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplMockitoTest {

	private static final String LOGIN = "ivanov";
	private static final String FULL_NAME = "Иванов Иван";
	private static final String EMAIL = "ivanov@test.ru";
	private static final String RAW_PASSWORD = "secret";
	private static final String ENCODED_PASSWORD = "bcrypt-hash";
	private static final String UNKNOWN_LOGIN = "unknown";

	@Mock
	private UserRepository userRepository;

	@Mock
	private MessageService messageService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	@DisplayName("Позитивный сценарий: создаёт пользователя с зашифрованным паролем и выбранной ролью")
	void createsUserWithEncodedPasswordAndSelectedRole() {
		when(userRepository.existsByLogin(LOGIN)).thenReturn(false);
		when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createUser(LOGIN, FULL_NAME, EMAIL, RAW_PASSWORD, UserRole.ADMIN);

		// Перехватывает пользователя, переданного сервисом в репозиторий.
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		// Проверяет единственный вызов save и сохраняет его аргумент в userCaptor.
		verify(userRepository).save(userCaptor.capture());
		User saved = userCaptor.getValue();
		Assertions.assertSame(saved, created);
		Assertions.assertEquals(LOGIN, saved.getLogin());
		Assertions.assertEquals(FULL_NAME, saved.getFullName());
		Assertions.assertEquals(EMAIL, saved.getEmail());
		Assertions.assertEquals(ENCODED_PASSWORD, saved.getPassword());
		Assertions.assertEquals(UserRole.ADMIN, saved.getRole());
		verify(passwordEncoder).encode(RAW_PASSWORD);
	}

	@Test
	@DisplayName("Негативный сценарий: не создаёт пользователя с уже занятым логином")
	void rejectsExistingLoginWithoutSavingUser() {
		when(userRepository.existsByLogin(LOGIN)).thenReturn(true);

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.createUser(LOGIN, FULL_NAME, EMAIL, RAW_PASSWORD, UserRole.USER));

		verify(messageService).get("user.login.exists", LOGIN);
		verify(userRepository, never()).existsByEmail(any());
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("Негативный сценарий: не создаёт пользователя с уже занятым email")
	void rejectsExistingEmailWithoutSavingUser() {
		when(userRepository.existsByLogin(LOGIN)).thenReturn(false);
		when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.createUser(LOGIN, FULL_NAME, EMAIL, RAW_PASSWORD, UserRole.USER));

		verify(messageService).get("user.email.exists", EMAIL);
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("Негативный сценарий: выбрасывает исключение при поиске несуществующего логина")
	void throwsExceptionForUnknownLogin() {
		when(userRepository.findByLogin(UNKNOWN_LOGIN)).thenReturn(Optional.empty());

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.findByLogin(UNKNOWN_LOGIN));

		verify(messageService).get("user.not-found", UNKNOWN_LOGIN);
	}

}
