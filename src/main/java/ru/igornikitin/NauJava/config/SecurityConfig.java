package ru.igornikitin.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Настройки безопасности приложения (Spring Security).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Пароли хранятся в БД в виде BCrypt-хеша. Этот же кодировщик Spring Security использует при входе:
	 * хеширует введённый пароль и сравнивает с хешем из БД.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
