package ru.igornikitin.NauJava.service;

import java.time.LocalDate;
import java.util.List;

import ru.igornikitin.NauJava.entity.Project;

public interface ProjectService {

	/**
	 * Создаёт проект; владелец автоматически становится участником с ролью OWNER.
	 * @param ownerLogin логин владельца — пользователь должен существовать
	 */
	Project createProject(String ownerLogin, String name, String description, LocalDate deadline);

	Project findById(Long id);

	List<Project> findAll();

	/** Удаляет проект вместе с участниками, задачами и комментариями к ним. */
	void deleteById(Long id);

	/**
	 * Изменяет поля проекта; параметр со значением null оставляет поле без изменений.
	 */
	void updateProject(Long id, String name, String description, LocalDate deadline);

	/** Добавляет существующего пользователя в проект с ролью MEMBER. */
	void addMember(Long projectId, String userLogin);

}
