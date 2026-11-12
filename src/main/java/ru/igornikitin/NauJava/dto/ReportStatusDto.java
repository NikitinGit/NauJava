package ru.igornikitin.NauJava.dto;

import ru.igornikitin.NauJava.entity.Report;
import ru.igornikitin.NauJava.entity.ReportStatus;

/** Состояние отчёта, которое можно отдать, пока HTML ещё не готов. */
public record ReportStatusDto(Long id, ReportStatus status, String message) {

	public static ReportStatusDto from(Report report, String message) {
		return new ReportStatusDto(report.getId(), report.getStatus(), message);
	}

}
