package ru.igornikitin.NauJava.console;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.service.ProjectService;

@Component
public class CommandProcessor {

	/** Слово без пробелов или текст в двойных/одинарных кавычках: create "Мой сайт" ... */
	private static final Pattern TOKEN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)");

	/** STRICT — чтобы несуществующие даты вроде 31.02.2026 не превращались молча в 28.02 */
	private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
			"uuuu-MM-dd", "uuuu.MM.dd", "dd.MM.uuuu", "dd-MM-uuuu", "dd/MM/uuuu").stream()
			.map(pattern -> DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT))
			.toList();

	private static final String CREATE_USAGE = "create <название> <срок> [описание]";
	private static final String GET_USAGE = "get <id>";
	private static final String UPDATE_USAGE = "update <id> name|description|deadline <значение>";
	private static final String ADD_MEMBER_USAGE = "add-member <id> <участник>";
	private static final String DELETE_USAGE = "delete <id>";

	private static final String HELP = """
			Команды:
			  create <название> <срок> [описание]                 создать проект ([...] — необязательно)
			  get <id>                                            показать проект
			  list                                                показать все проекты
			  update <id> name|description|deadline <значение>    изменить поле проекта
			  add-member <id> <участник>                          добавить участника
			  delete <id>                                         удалить проект
			  help                                                эта справка
			  exit                                                выход
			Срок: 2026-12-31, 2026.12.31, 31.12.2026, 31-12-2026 или 31/12/2026
			Текст с пробелами берите в кавычки: create "Мой сайт" 31.12.2026 "Сайт компании\"""";

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
					checkArgs(cmd, 3, 4, CREATE_USAGE);
					String description = cmd.size() > 3 ? cmd.get(3) : null;
					Project project = projectService.createProject(cmd.get(1), description, parseDate(cmd.get(2)));
					System.out.println("Проект успешно создан, id=" + project.getId());
				}
				case "get" -> {
					checkArgs(cmd, 2, 2, GET_USAGE);
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
					checkArgs(cmd, 4, 4, UPDATE_USAGE);
					update(parseId(cmd.get(1)), cmd.get(2), cmd.get(3));
					System.out.println("Проект успешно изменён");
				}
				case "add-member" -> {
					checkArgs(cmd, 3, 3, ADD_MEMBER_USAGE);
					projectService.addMember(parseId(cmd.get(1)), cmd.get(2));
					System.out.println("Участник успешно добавлен");
				}
				case "delete" -> {
					checkArgs(cmd, 2, 2, DELETE_USAGE);
					projectService.deleteById(parseId(cmd.get(1)));
					System.out.println("Проект успешно удалён");
				}
				case "help" -> System.out.println(HELP);
				default -> System.out.println("Введена неизвестная команда. Список команд: help");
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Ошибка: " + e.getMessage());
		}
	}

	private void update(Long id, String field, String value) {
		switch (field) {
			case "name" -> projectService.updateProject(id, value, null, null);
			case "description" -> projectService.updateProject(id, null, value, null);
			case "deadline" -> projectService.updateProject(id, null, null, parseDate(value));
			default -> throw new IllegalArgumentException("неизвестное поле " + field + ", доступны: name, description, deadline");
		}
	}

	private static List<String> tokenize(String input) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = TOKEN.matcher(input);
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				tokens.add(matcher.group(1));
			} else if (matcher.group(2) != null) {
				tokens.add(matcher.group(2));
			} else {
				tokens.add(matcher.group(3));
			}
		}
		return tokens;
	}

	/** Проверяет число аргументов вместе с самой командой: от min до max. */
	private static void checkArgs(List<String> cmd, int min, int max, String usage) {
		if (cmd.size() < min) {
			throw new IllegalArgumentException("не хватает аргументов. Формат: " + usage);
		}
		if (cmd.size() > max) {
			throw new IllegalArgumentException("лишние аргументы. Формат: " + usage
					+ ". Текст с пробелами берите в кавычки");
		}
	}

	private static Long parseId(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("id должен быть числом, получено: " + value);
		}
	}

	private static LocalDate parseDate(String value) {
		for (DateTimeFormatter format : DATE_FORMATS) {
			try {
				return LocalDate.parse(value, format);
			} catch (DateTimeParseException e) {
				// пробуем следующий формат
			}
		}
		throw new IllegalArgumentException("не удалось распознать дату '" + value
				+ "'. Примеры: 2026-12-31, 31.12.2026. Если в названии есть пробелы — возьмите его в кавычки");
	}

}
