package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ru.igornikitin.NauJava.entity.Project;

@RepositoryRestResource(path = "projects")
public interface ProjectRepository extends CrudRepository<Project, Long> {

}
