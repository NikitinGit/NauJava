package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;

import ru.igornikitin.NauJava.entity.Comment;

public interface CommentRepository extends CrudRepository<Comment, Long> {

	/** Удаляет комментарии ко всем задачам проекта (Comment → Task → Project). */
	void deleteByTaskProjectId(Long projectId);

}
