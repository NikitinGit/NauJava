package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import ru.igornikitin.NauJava.entity.ProjectMember;

@RepositoryRestResource(path = "project-members")
public interface ProjectMemberRepository extends CrudRepository<ProjectMember, Long> {

	boolean existsByProjectIdAndUserId(Long projectId, Long userId);

	// служебный метод для каскадного удаления проекта; через REST не выставляется, иначе он стал бы GET /search/...
	@RestResource(exported = false)
	void deleteByProjectId(Long projectId);

}
