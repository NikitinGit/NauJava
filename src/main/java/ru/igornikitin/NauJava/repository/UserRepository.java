package ru.igornikitin.NauJava.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ru.igornikitin.NauJava.entity.User;

@RepositoryRestResource(path = "users")
public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByLogin(String login);

	boolean existsByLogin(String login);

	boolean existsByEmail(String email);

}
