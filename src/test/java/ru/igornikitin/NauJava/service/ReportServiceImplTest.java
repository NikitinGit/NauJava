package ru.igornikitin.NauJava.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Report;
import ru.igornikitin.NauJava.entity.ReportStatus;
import ru.igornikitin.NauJava.entity.User;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.ReportRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/** Быстрые unit-тесты формирования отчёта: без подключения к PostgreSQL. */
class ReportServiceImplTest {

	@Test
	void testCreateReportHasCreatedStatus() {
		ReportRepository reports = mock(ReportRepository.class);
		when(reports.save(any(Report.class))).thenAnswer(invocation -> {
			Report report = invocation.getArgument(0);
			report.setId(1L);
			return report;
		});
		ReportService service = new ReportServiceImpl(reports, mock(UserRepository.class), mock(ProjectRepository.class),
				new SpringTemplateEngine());

		Long id = service.createReport();

		Assertions.assertEquals(1L, id);
	}

	@Test
	void testAsyncGenerationStoresHtmlAndCompletedStatus() {
		Report report = new Report();
		report.setId(7L);
		ReportRepository reports = mock(ReportRepository.class);
		when(reports.findById(7L)).thenReturn(Optional.of(report));
		when(reports.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserRepository users = mock(UserRepository.class);
		when(users.count()).thenReturn(3L);
		ProjectRepository projects = mock(ProjectRepository.class);
		when(projects.findAllWithOwner()).thenReturn(List.of(project("<Проект>")));
		ReportService service = new ReportServiceImpl(reports, users, projects, templateEngine());

		service.generateReportAsync(7L).join();

		Assertions.assertEquals(ReportStatus.COMPLETED, report.getStatus());
		Assertions.assertNotNull(report.getContent());
		Assertions.assertTrue(report.getContent().contains("Количество зарегистрированных пользователей"));
		Assertions.assertTrue(report.getContent().contains(">3</td>"));
		Assertions.assertTrue(report.getContent().contains("&lt;Проект&gt;"));
		Assertions.assertTrue(report.getContent().contains("Общее время формирования отчёта"));
	}

	@Test
	void testFailedGenerationMarksReportAsError() {
		Report report = new Report();
		report.setId(8L);
		ReportRepository reports = mock(ReportRepository.class);
		when(reports.findById(8L)).thenReturn(Optional.of(report));
		doAnswer(invocation -> invocation.getArgument(0)).when(reports).save(any(Report.class));
		UserRepository users = mock(UserRepository.class);
		when(users.count()).thenThrow(new IllegalStateException("БД недоступна"));
		ProjectRepository projects = mock(ProjectRepository.class);
		when(projects.findAllWithOwner()).thenReturn(List.of());
		ReportService service = new ReportServiceImpl(reports, users, projects, templateEngine());

		service.generateReportAsync(8L).join();

		Assertions.assertEquals(ReportStatus.ERROR, report.getStatus());
		Assertions.assertThrows(IllegalStateException.class, () -> service.getContent(8L));
	}

	private static Project project(String name) {
		User owner = new User();
		owner.setLogin("owner");
		owner.setFullName("Владелец");
		Project project = new Project();
		project.setId(10L);
		project.setName(name);
		project.setDeadline(LocalDate.of(2030, 1, 31));
		project.setOwner(owner);
		return project;
	}

	private static SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		org.thymeleaf.templateresolver.ClassLoaderTemplateResolver resolver =
				new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		engine.setTemplateResolver(resolver);
		return engine;
	}

}
