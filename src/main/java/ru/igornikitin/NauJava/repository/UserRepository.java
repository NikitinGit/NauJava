package ru.igornikitin.NauJava.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import ru.igornikitin.NauJava.entity.User;

@RepositoryRestResource(path = "users")
public interface UserRepository extends CrudRepository<User, Long> {

	/**
	 * Через Data REST создавать и изменять пользователей нельзя (POST/PUT/PATCH отключены):
	 * иначе пароль сохранился бы без шифрования в обход UserService. Пользователи создаются регистрацией.
	 */
	@Override
	@RestResource(exported = false)
	<S extends User> S save(S entity);

	Optional<User> findByLogin(String login);

	boolean existsByLogin(String login);

	boolean existsByEmail(String email);

}
