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
		when(userRepository.existsByLogin("ivanov")).thenReturn(false);
		when(userRepository.existsByEmail("ivanov@test.ru")).thenReturn(false);
		when(passwordEncoder.encode("secret")).thenReturn("bcrypt-hash");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createUser("ivanov", "Иванов Иван", "ivanov@test.ru", "secret", UserRole.ADMIN);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User saved = userCaptor.getValue();
		Assertions.assertSame(saved, created);
		Assertions.assertEquals("ivanov", saved.getLogin());
		Assertions.assertEquals("Иванов Иван", saved.getFullName());
		Assertions.assertEquals("ivanov@test.ru", saved.getEmail());
		Assertions.assertEquals("bcrypt-hash", saved.getPassword());
		Assertions.assertEquals(UserRole.ADMIN, saved.getRole());
		verify(passwordEncoder).encode("secret");
	}

	@Test
	@DisplayName("Негативный сценарий: не создаёт пользователя с уже занятым логином")
	void rejectsExistingLoginWithoutSavingUser() {
		when(userRepository.existsByLogin("ivanov")).thenReturn(true);

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.createUser("ivanov", "Иванов Иван", "ivanov@test.ru", "secret", UserRole.USER));

		verify(messageService).get("user.login.exists", "ivanov");
		verify(userRepository, never()).existsByEmail(any());
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("Негативный сценарий: не создаёт пользователя с уже занятым email")
	void rejectsExistingEmailWithoutSavingUser() {
		when(userRepository.existsByLogin("ivanov")).thenReturn(false);
		when(userRepository.existsByEmail("ivanov@test.ru")).thenReturn(true);

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.createUser("ivanov", "Иванов Иван", "ivanov@test.ru", "secret", UserRole.USER));

		verify(messageService).get("user.email.exists", "ivanov@test.ru");
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("Негативный сценарий: выбрасывает исключение при поиске несуществующего логина")
	void throwsExceptionForUnknownLogin() {
		when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> userService.findByLogin("unknown"));

		verify(messageService).get("user.not-found", "unknown");
	}

}
