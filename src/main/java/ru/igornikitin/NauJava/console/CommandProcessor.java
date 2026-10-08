package ru.igornikitin.NauJava.console;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.service.ProjectService;

@Component
public class CommandProcessor {

	/** Слово без пробелов или текст в двойных кавычках: create "Мой сайт" ... */
	private static final Pattern TOKEN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

	private static final String HELP = """
			Команды:
			  create <название> <срок ГГГГ-ММ-ДД> <описание>       создать проект
			  get <id>                                            показать проект
			  list                                                показать все проекты
			  update <id> name|description|deadline <значение>    изменить поле проекта
			  add-member <id> <участник>                          добавить участника
			  delete <id>                                         удалить проект
			  help                                                эта справка
			  exit                                                выход
			Текст с пробелами берите в кавычки: create "Мой сайт" 2026-12-31 "Сайт компании\"""";

	private final ProjectService projectService;

	public CommandProcessor(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void processCommand(String input) {
		List<String> cmd = tokenize(input);
		if (cmd.isEmpty()) {
			return;
		}
		try {
			switch (cmd.get(0)) {
				case "create" -> {
					requireArgs(cmd, 4);
					Project project = projectService.createProject(cmd.get(1), cmd.get(3), LocalDate.parse(cmd.get(2)));
					System.out.println("Проект успешно создан, id=" + project.getId());
				}
				case "get" -> {
					requireArgs(cmd, 2);
					System.out.println(projectService.findById(parseId(cmd.get(1))));
				}
				case "list" -> {
					List<Project> projects = projectService.findAll();
					if (projects.isEmpty()) {
						System.out.println("Проектов нет");
					}
					projects.forEach(System.out::println);
				}
				case "update" -> {
					requireArgs(cmd, 4);
					update(parseId(cmd.get(1)), cmd.get(2), cmd.get(3));
					System.out.println("Проект успешно изменён");
				}
				case "add-member" -> {
					requireArgs(cmd, 3);
					projectService.addMember(parseId(cmd.get(1)), cmd.get(2));
					System.out.println("Участник успешно добавлен");
				}
				case "delete" -> {
					requireArgs(cmd, 2);
					projectService.deleteById(parseId(cmd.get(1)));
					System.out.println("Проект успешно удалён");
				}
				case "help" -> System.out.println(HELP);
				default -> System.out.println("Введена неизвестная команда. Список команд: help");
			}
		} catch (DateTimeParseException e) {
			System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД, например 2026-12-31");
		} catch (IllegalArgumentException e) {
			System.out.println("Ошибка: " + e.getMessage());
		}
	}

	private void update(Long id, String field, String value) {
		switch (field) {
			case "name" -> projectService.updateProject(id, value, null, null);
			case "description" -> projectService.updateProject(id, null, value, null);
			case "deadline" -> projectService.updateProject(id, null, null, LocalDate.parse(value));
			default -> throw new IllegalArgumentException("неизвестное поле " + field + ", доступны: name, description, deadline");
		}
	}

	private static List<String> tokenize(String input) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = TOKEN.matcher(input);
		while (matcher.find()) {
			tokens.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
		}
		return tokens;
	}

	private static void requireArgs(List<String> cmd, int count) {
		if (cmd.size() < count) {
			throw new IllegalArgumentException("не хватает аргументов. Формат команд: help");
		}
	}

	private static Long parseId(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("id должен быть числом, получено: " + value);
		}
	}

}
