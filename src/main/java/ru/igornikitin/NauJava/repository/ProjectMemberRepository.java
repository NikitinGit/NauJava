package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.ProjectMember;

public interface ProjectMemberRepository extends CrudRepository<ProjectMember, Long> {

	boolean existsByProjectIdAndUserId(Long projectId, Long userId);

	void deleteByProjectId(Long projectId);

}
