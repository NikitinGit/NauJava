package ru.igornikitin.NauJava.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Загружает пользователя приложения из БД по логину и преобразует его в UserDetails для Spring Security.
 * Наш User (сущность) и org.springframework.security.core.userdetails.User — разные классы,
 * поэтому второй указан полным именем.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	private final MessageService messageService;

	public UserDetailsServiceImpl(UserRepository userRepository, MessageService messageService) {
		this.userRepository = userRepository;
		this.messageService = messageService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User appUser = userRepository.findByLogin(username)
				.orElseThrow(() -> new UsernameNotFoundException(messageService.get("user.not-found", username)));
		return new org.springframework.security.core.userdetails.User(appUser.getLogin(), appUser.getPassword(),
				mapRoles(appUser));
	}

	/** Роль в Spring Security — это право (GrantedAuthority) с префиксом ROLE_: USER → ROLE_USER. */
	private Collection<GrantedAuthority> mapRoles(User appUser) {
		return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
	}

}
