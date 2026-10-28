package ru.igornikitin.NauJava.service;

import java.util.List;

import ru.igornikitin.NauJava.entity.User;

public interface UserService {

	/** Создаёт пользователя с ролью USER. */
	User createUser(String login, String fullName, String email, String password);

	/** Возвращает пользователя по логину (имени пользователя). */
	User findByLogin(String login);

	List<User> findAll();

}
