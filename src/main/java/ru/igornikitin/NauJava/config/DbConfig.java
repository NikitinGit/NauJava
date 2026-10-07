package ru.igornikitin.NauJava.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ru.igornikitin.NauJava.entity.Project;

@Configuration
public class DbConfig {

	@Bean
	@Scope(value = BeanDefinition.SCOPE_SINGLETON)
	public List<Project> projectContainer() {
		return new ArrayList<>();
	}

}
