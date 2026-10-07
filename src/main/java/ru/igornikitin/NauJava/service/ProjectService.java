package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
import java.util.List;

import ru.igornikitin.NauJava.entity.Project;

public interface ProjectService {

	void createProject(Long id, String name, String description, LocalDate deadline);

	Project findById(Long id);

	List<Project> findAll();

	void deleteById(Long id);

	/**
	 * Изменяет поля проекта; параметр со значением null оставляет поле без изменений.
	 */
	void updateProject(Long id, String name, String description, LocalDate deadline);

	void addMember(Long projectId, String member);

}
