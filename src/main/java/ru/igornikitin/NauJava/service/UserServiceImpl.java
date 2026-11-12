package ru.igornikitin.NauJava.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;
import ru.igornikitin.NauJava.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final MessageService messageService;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, MessageService messageService,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.messageService = messageService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public User createUser(String login, String fullName, String email, String password, UserRole role) {
		requireNotBlank(login, "user.login.blank");
		requireNotBlank(fullName, "user.full-name.blank");
		requireNotBlank(email, "user.email.blank");
		requireNotBlank(password, "user.password.blank");
		// проверяем заранее, чтобы вместо ошибки уникального индекса БД выдать понятное сообщение
		if (userRepository.existsByLogin(login)) {
			throw new IllegalArgumentException(messageService.get("user.login.exists", login));
		}
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException(messageService.get("user.email.exists", email));
		}
		User user = new User();
		user.setLogin(login);
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setRole(role);
		return userRepository.save(user);
	}

	@Override
	public User findByLogin(String login) {
		return userRepository.findByLogin(login)
				.orElseThrow(() -> new IllegalArgumentException(messageService.get("user.not-found", login)));
	}

	@Override
	public List<User> findAll() {
		List<User> users = new ArrayList<>();
		userRepository.findAll().forEach(users::add);
		return users;
	}

	private void requireNotBlank(String value, String messageCode) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(messageService.get(messageCode));
		}
	}

}
