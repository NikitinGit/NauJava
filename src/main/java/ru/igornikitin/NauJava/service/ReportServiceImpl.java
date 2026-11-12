package ru.igornikitin.NauJava.service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import ru.igornikitin.NauJava.entity.Project;
import ru.igornikitin.NauJava.entity.Report;
import ru.igornikitin.NauJava.entity.ReportStatus;
import ru.igornikitin.NauJava.repository.ProjectRepository;
import ru.igornikitin.NauJava.repository.ReportRepository;
import ru.igornikitin.NauJava.repository.UserRepository;

@Service
public class ReportServiceImpl implements ReportService {

	private final ReportRepository reportRepository;

	private final UserRepository userRepository;

	private final ProjectRepository projectRepository;

	private final SpringTemplateEngine templateEngine;

	public ReportServiceImpl(ReportRepository reportRepository, UserRepository userRepository,
			ProjectRepository projectRepository, SpringTemplateEngine templateEngine) {
		this.reportRepository = reportRepository;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.templateEngine = templateEngine;
	}

	@Override
	public Long createReport() {
		Report report = new Report();
		report.setStatus(ReportStatus.CREATED);
		return reportRepository.save(report).getId();
	}

	@Override
	public CompletableFuture<Void> generateReportAsync(Long reportId) {
		return CompletableFuture.runAsync(() -> generateReport(reportId));
	}

	@Override
	public Report findById(Long reportId) {
		return reportRepository.findById(reportId)
				.orElseThrow(() -> new IllegalArgumentException("Отчёт с id " + reportId + " не найден"));
	}

	@Override
	public String getContent(Long reportId) {
		Report report = findById(reportId);
		if (report.getStatus() != ReportStatus.COMPLETED) {
			throw new IllegalStateException("Отчёт ещё не сформирован или завершился с ошибкой");
		}
		return report.getContent();
	}

	private void generateReport(Long reportId) {
		try {
			Report report = findById(reportId);
			long reportStartedAt = System.currentTimeMillis();
			long[] userCount = new long[1];
			long[] usersElapsed = new long[1];
			@SuppressWarnings("unchecked")
			List<Project>[] projects = new List[1];
			long[] projectsElapsed = new long[1];
			AtomicReference<Throwable> error = new AtomicReference<>();

			Thread usersThread = new Thread(() -> {
				try {
					long startedAt = System.currentTimeMillis();
					userCount[0] = userRepository.count();
					usersElapsed[0] = System.currentTimeMillis() - startedAt;
				} catch (Throwable ex) {
					error.compareAndSet(null, ex);
				}
			}, "report-users-" + reportId);
			Thread projectsThread = new Thread(() -> {
				try {
					long startedAt = System.currentTimeMillis();
					projects[0] = projectRepository.findAllWithOwner();
					projectsElapsed[0] = System.currentTimeMillis() - startedAt;
				} catch (Throwable ex) {
					error.compareAndSet(null, ex);
				}
			}, "report-projects-" + reportId);

			usersThread.start();
			projectsThread.start();
			usersThread.join();
			projectsThread.join();

			if (error.get() != null) {
				throw new IllegalStateException("Не удалось собрать данные для отчёта", error.get());
			}
			report.setContent(toHtml(userCount[0], usersElapsed[0], projects[0], projectsElapsed[0],
					System.currentTimeMillis() - reportStartedAt));
			report.setStatus(ReportStatus.COMPLETED);
			reportRepository.save(report);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			markAsError(reportId);
		} catch (RuntimeException ex) {
			markAsError(reportId);
		}
	}

	private void markAsError(Long reportId) {
		reportRepository.findById(reportId).ifPresent(report -> {
			report.setStatus(ReportStatus.ERROR);
			reportRepository.save(report);
		});
	}

	private String toHtml(long userCount, long usersElapsed, List<Project> projects, long projectsElapsed,
			long totalElapsed) {
		Context context = new Context(Locale.forLanguageTag("ru"));
		context.setVariable("userCount", userCount);
		context.setVariable("usersElapsed", usersElapsed);
		context.setVariable("projects", projects);
		context.setVariable("projectsElapsed", projectsElapsed);
		context.setVariable("totalElapsed", totalElapsed);
		return templateEngine.process("report", context);
	}

}
