package ru.igornikitin.NauJava.service;

import java.util.List;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.entity.UserRole;

public interface UserService {

	/** Создаёт пользователя с ролью USER. */
	default User createUser(String login, String fullName, String email, String password) {
		return createUser(login, fullName, email, password, UserRole.USER);
	}

	/**
	 * Создаёт пользователя с заданной ролью; пароль сохраняется в зашифрованном виде.
	 * @param password пароль в открытом виде
	 */
	User createUser(String login, String fullName, String email, String password, UserRole role);

	/** Возвращает пользователя по логину (имени пользователя). */
	User findByLogin(String login);

	List<User> findAll();

}
