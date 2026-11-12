package ru.igornikitin.NauJava.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Тексты сообщений из src/main/resources/messages/messages.properties.
 * Бин MessageSource создаёт Spring Boot по настройкам spring.messages.* в application.properties.
 */
@Service
public class MessageService {

	private final MessageSource messageSource;

	public MessageService(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String get(String code, Object... args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

}