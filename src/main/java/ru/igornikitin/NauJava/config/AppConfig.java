package ru.igornikitin.NauJava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Value("${app.name}")
	private String appName;

	@Value("${app.version}")
	private String appVersion;

	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

}
