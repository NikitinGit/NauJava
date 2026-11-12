package ru.igornikitin.NauJava.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;

import ru.igornikitin.NauJava.entity.Report;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.ReportRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

/**
 * Учебное сравнение mock и spy на одном методе createReport(), без Spring и БД.
 * Для Debug поставьте breakpoint в ReportServiceImpl.createReport():
 * он сработает в сценариях с ручным и аннотированным spy, где метод не подменён.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceMockSpyExampleTest {

	private static final Long STUBBED_REPORT_ID = 100L;
	private static final Long SAVED_REPORT_ID = 255L;

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private SpringTemplateEngine templateEngine;

	private ReportServiceImpl mockedService;

	private ReportServiceImpl spiedService;

	// Mockito создаёт реализацию, передаёт @Mock-зависимости в конструктор и делает spy.
	@Spy
	@InjectMocks
	private ReportServiceImpl annotatedSpiedService;

	@BeforeEach
	void setUp() {
		// еще один способ создать мок
		mockedService = mock(ReportServiceImpl.class);
		// Spy оборачивает реализацию с настоящим кодом, а зависимости остаются моками.
		spiedService = spy(new ReportServiceImpl(reportRepository, userRepository, projectRepository, templateEngine));
	}

	@Test
	@DisplayName("Mock возвращает заглушку, не выполняя тело createReport")
	void mockReturnsStubWithoutCallingRealMethod() {
		Assertions.assertEquals(0L, mockedService.createReport()); // Значение Long по умолчанию у Mockito.
		doReturn(STUBBED_REPORT_ID).when(mockedService).createReport();

		Long id = mockedService.createReport(); // Breakpoint в реальном методе не сработает.

		Assertions.assertEquals(STUBBED_REPORT_ID, id);
		verifyNoInteractions(reportRepository);
	}

	@Test
	@DisplayName("Spy без заглушки выполняет настоящий createReport и вызывает репозиторий")
	void spyCallsRealMethodWhenNotStubbed() {
		Report savedReport = new Report();
		savedReport.setId(SAVED_REPORT_ID);
		when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

		Long id = spiedService.createReport(); // Здесь breakpoint в createReport сработает.

		Assertions.assertEquals(SAVED_REPORT_ID, id);
		verify(reportRepository).save(any(Report.class));
	}

	@Test
	@DisplayName("@Spy вместе с @InjectMocks выполняет настоящий createReport с мок-зависимостями")
	void annotatedSpyCallsRealMethodWhenNotStubbed() {
		Report savedReport = new Report();
		savedReport.setId(SAVED_REPORT_ID);
		when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

		Long id = annotatedSpiedService.createReport(); // Breakpoint в createReport тоже сработает.

		Assertions.assertEquals(SAVED_REPORT_ID, id);
		verify(reportRepository).save(any(Report.class));
	}

	@Test
	@DisplayName("Spy с doReturn возвращает заглушку, не выполняя настоящий createReport")
	void stubbedSpyDoesNotCallRealMethod() {
		// doReturn подменяет метод spy: настоящее тело не вызывается даже при настройке.
		doReturn(STUBBED_REPORT_ID).when(spiedService).createReport();

		Long id = spiedService.createReport(); // Breakpoint в реальном методе не сработает.

		Assertions.assertEquals(STUBBED_REPORT_ID, id);
		verifyNoInteractions(reportRepository);
	}

}
