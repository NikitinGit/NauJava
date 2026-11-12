package ru.igornikitin.NauJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ru.igornikitin.NauJava.entity.Project;

@RepositoryRestResource(path = "projects")
public interface ProjectRepository extends CrudRepository<Project, Long> {

	/** Владелец нужен при построении отчёта и загружается одним запросом, до выхода из потока. */
	@Query("SELECT p FROM Project p JOIN FETCH p.owner ORDER BY p.id")
	List<Project> findAllWithOwner();

}
