package ru.igornikitin.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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

	/**
	 * Правила доступа: вход и регистрация открыты всем, Swagger и actuator — только ADMIN,
	 * всё остальное (REST API, HTML-страницы) — только после входа; без входа — перенаправление на /login.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		http.authorizeHttpRequests(authorize -> authorize
						// /error — чтобы страница ошибки (например, 403) показывалась, а не тоже требовала входа
						.requestMatchers("/login", "/registration", "/error").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").hasRole("ADMIN")
						.requestMatchers("/actuator/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						// после входа — на запрошенную ранее страницу, а если её не было — на список проектов
						.defaultSuccessUrl("/custom/projects/view/list")
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				// CSRF-токен в cookie XSRF-TOKEN: его читает Swagger UI (springdoc.swagger-ui.csrf.enabled)
				// и отправляет в заголовке, иначе POST/PUT/PATCH/DELETE из Swagger получали бы 403
				.csrf(CsrfConfigurer::spa);
		return http.build();
	}

}
