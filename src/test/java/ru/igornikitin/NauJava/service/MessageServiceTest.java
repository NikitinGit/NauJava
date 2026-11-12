package ru.igornikitin.NauJava.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Тексты из messages/messages.properties: кириллица читается в UTF-8, параметры подставляются.
 */
@SpringBootTest
class MessageServiceTest {

	private final MessageService messageService;

	@Autowired
	MessageServiceTest(MessageService messageService) {
		this.messageService = messageService;
	}

	@Test
	void testMessageWithParameter() {
		Assertions.assertEquals("Пользователь с логином ivanov уже существует",
				messageService.get("user.login.exists", "ivanov"));
	}

	/**
	 * Число выводится без разделителя разрядов: «1234», а не «1 234».
	 */
	@Test
	void testMessageWithNumberParameters() {
		Assertions.assertEquals("Проект с id 1234 не найден", messageService.get("project.not-found", 1234L));
		Assertions.assertEquals("Участник ivanov уже есть в проекте 5678",
				messageService.get("project.member.exists", "ivanov", 5678L));
	}

}
