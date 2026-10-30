package ru.igornikitin.NauJava.service;

import java.util.List;

import ru.igornikitin.NauJava.entity.User;

public interface UserService {

	User createUser(String login, String fullName, String email);

	User findByLogin(String login);

	List<User> findAll();

}
