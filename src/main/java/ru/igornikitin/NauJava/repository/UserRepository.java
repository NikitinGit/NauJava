package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
