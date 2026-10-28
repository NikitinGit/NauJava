package ru.igornikitin.NauJava.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User createUser(String login, String fullName, String email, String password) {
		requireNotBlank(login, "логин");
		requireNotBlank(fullName, "ФИО");
		requireNotBlank(email, "email");
		requireNotBlank(password, "пароль");
		// проверяем заранее, чтобы вместо ошибки уникального индекса БД выдать понятное сообщение
		if (userRepository.existsByLogin(login)) {
			throw new IllegalArgumentException("пользователь с логином " + login + " уже существует");
		}
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("пользователь с email " + email + " уже существует");
		}
		User user = new User();
		user.setLogin(login);
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(password);
		return userRepository.save(user);
	}

	@Override
	public User findByLogin(String login) {
		return userRepository.findByLogin(login)
				.orElseThrow(() -> new IllegalArgumentException("пользователь с логином " + login + " не найден"));
	}

	@Override
	public List<User> findAll() {
		List<User> users = new ArrayList<>();
		userRepository.findAll().forEach(users::add);
		return users;
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " не может быть пустым");
		}
	}

}
