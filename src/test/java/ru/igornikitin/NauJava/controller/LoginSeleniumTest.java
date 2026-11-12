package ru.igornikitin.NauJava.controller;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.bonigarcia.wdm.WebDriverManager;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * E2E-проверки формы входа через настоящий браузер Chrome и Spring Security.
 * В отличие от API-теста безопасность здесь включена: проверяется реальный вход и выход пользователя.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "app.console.enabled=false", "app.security.enabled=true" })
class LoginSeleniumTest {

	private static final String RAW_PASSWORD = "test-password";
	private static final String PROJECTS_PATH = "/custom/projects/view/list";

	@LocalServerPort
	private int port;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private WebDriver driver;

	private WebDriverWait wait;

	private User createdUser;

	@Autowired
	LoginSeleniumTest(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@BeforeEach
	void setUp() {
		// На NixOS можно указать системный драйвер через -Dwebdriver.chrome.driver=/путь/к/chromedriver.
		if (System.getProperty("webdriver.chrome.driver") == null) {
			WebDriverManager.chromedriver().setup();
		}
		ChromeOptions options = new ChromeOptions();
		// Позволяет выбрать Chromium из того же набора пакетов Nix, что и ChromeDriver.
		String chromeBinary = System.getProperty("test.chrome.binary");
		if (chromeBinary != null && !chromeBinary.isBlank()) {
			options.setBinary(chromeBinary);
		}
		options.addArguments("--window-size=1280,800", "--disable-dev-shm-usage");
		// открывается браузер
		driver = new ChromeDriver(options);
		// ожидает нужное состояние страницы до 10 секунд, продолжая сразу после его появления.
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		createdUser = createUser();
	}

	@AfterEach
	void cleanUp() {
		try {
			if (driver != null) {
				driver.quit();
			}
		} finally {
			if (createdUser != null) {
				userRepository.delete(createdUser);
			}
		}
	}

	@Test
	@DisplayName("Пользователь с корректными учётными данными входит в систему")
	void logsInWithCorrectCredentials() {
		logIn();

		Assertions.assertEquals("Список проектов", driver.getTitle());
	}

	@Test
	@DisplayName("Пользователь после входа может выйти из системы")
	void logsOutAfterSuccessfulLogin() {
		logIn();
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("form[action='/logout'] input[type='submit']")))
				.click();

		wait.until(ExpectedConditions.urlToBe(baseUrl() + "/login?logout"));
		Assertions.assertTrue(driver.findElement(By.tagName("body")).getText().contains("Вы вышли из системы"));

		// После выхода защищённая страница снова должна требовать входа.
		driver.get(baseUrl() + PROJECTS_PATH);
		wait.until(ExpectedConditions.urlToBe(baseUrl() + "/login"));
		Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
	}

	private void logIn() {
		driver.get(baseUrl() + "/login");
		driver.findElement(By.name("username")).sendKeys(createdUser.getLogin());
		driver.findElement(By.name("password")).sendKeys(RAW_PASSWORD);
		driver.findElement(By.cssSelector("input[type='submit']")).click();
		wait.until(ExpectedConditions.urlToBe(baseUrl() + PROJECTS_PATH));
		wait.until(ExpectedConditions.textToBe(By.cssSelector("body > div > b"), createdUser.getLogin()));
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private User createUser() {
		String unique = UUID.randomUUID().toString();
		User user = new User();
		user.setLogin(unique.substring(0, 20));
		user.setFullName("Пользователь Selenium-теста");
		user.setEmail(unique + "@test.ru");
		user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
		return userRepository.save(user);
	}

}
