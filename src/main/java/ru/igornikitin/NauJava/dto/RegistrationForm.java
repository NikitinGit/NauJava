package ru.igornikitin.NauJava.dto;

/**
 * Данные формы регистрации. Отдельный класс вместо сущности User (как в примере методички):
 * при привязке к User можно было бы передать в запросе лишнее поле role=ADMIN и стать администратором.
 */
public record RegistrationForm(String login, String fullName, String email, String password) {

	public static RegistrationForm empty() {
		return new RegistrationForm(null, null, null, null);
	}

}
