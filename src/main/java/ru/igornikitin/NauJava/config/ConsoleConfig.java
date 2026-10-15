package ru.igornikitin.NauJava.config;

import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.igornikitin.NauJava.console.CommandProcessor;

@Configuration
public class ConsoleConfig {

	// В тестах консоль отключается (app.console.enabled=false): иначе цикл ждёт ввода и сборка зависает.
	// @Disabled на тесте тоже решил бы проблему, но тогда тест не проверял бы, что контекст Spring поднимается.
	@Bean
	@ConditionalOnBooleanProperty(name = "app.console.enabled", matchIfMissing = true)
	public CommandLineRunner commandScanner(CommandProcessor commandProcessor, ApplicationContext context) {
		return args -> {
			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Введите команду. 'help' — список команд, 'exit' — выход.");
				while (true) {
					System.out.print("> ");
					if (!scanner.hasNextLine()) {
						// ввод закрыт (например, при запуске тестов) — просто выходим из цикла
						return;
					}
					String input = scanner.nextLine();
					if ("exit".equalsIgnoreCase(input.trim())) {
						System.out.println("Выход из программы...");
						break;
					}
					commandProcessor.processCommand(input);
				}
			}
			// веб-сервер (нужен для actuator) сам не завершится, поэтому останавливаем приложение явно
			System.exit(SpringApplication.exit(context));
		};
	}
}
