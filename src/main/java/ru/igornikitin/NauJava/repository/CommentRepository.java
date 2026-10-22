package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import ru.igornikitin.NauJava.entity.Comment;

@RepositoryRestResource(path = "comments")
public interface CommentRepository extends CrudRepository<Comment, Long> {

	/** Удаляет комментарии ко всем задачам проекта (Comment → Task → Project). */
	@RestResource(exported = false)
	void deleteByTaskProjectId(Long projectId);

}
