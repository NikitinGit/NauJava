package ru.igornikitin.NauJava.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.igornikitin.NauJava.dto.ReportCreatedDto;
import ru.igornikitin.NauJava.dto.ReportStatusDto;
import ru.igornikitin.NauJava.entity.Report;
import ru.igornikitin.NauJava.entity.ReportStatus;
import ru.igornikitin.NauJava.service.ReportService;

/** REST API для запуска формирования и получения HTML-отчётов. */
@RestController
@RequestMapping("/custom/reports")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	/** Создаёт отчёт и запускает его в фоне — ожидания формирования здесь нет. */
	@PostMapping
	public ResponseEntity<ReportCreatedDto> create() {
		Long reportId = reportService.createReport();
		reportService.generateReportAsync(reportId);
		return ResponseEntity.accepted().body(new ReportCreatedDto(reportId, ReportStatus.CREATED));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getReport(@PathVariable Long id) {
		Report report = reportService.findById(id);
		if (report.getStatus() == ReportStatus.CREATED) {
			return ResponseEntity.accepted().body(ReportStatusDto.from(report, "Отчёт ещё формируется"));
		}
		if (report.getStatus() == ReportStatus.ERROR) {
			return ResponseEntity.internalServerError().body(ReportStatusDto.from(report,
					"При формировании отчёта произошла ошибка"));
		}

		return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(report.getContent());
	}

}
