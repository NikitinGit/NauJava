package ru.igornikitin.NauJava.entity;

/**
 * Роль пользователя в системе (для Spring Security).
 * Не путать с MemberRole — ролью участника внутри конкретного проекта.
 */
public enum UserRole {

	USER,
	ADMIN

}
