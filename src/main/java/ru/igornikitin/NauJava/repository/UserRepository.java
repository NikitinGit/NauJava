package ru.igornikitin.NauJava.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByLogin(String login);

	boolean existsByLogin(String login);

	boolean existsByEmail(String email);

}
