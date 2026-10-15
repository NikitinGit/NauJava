package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.Project;

public interface ProjectRepository extends CrudRepository<Project, Long> {

}
