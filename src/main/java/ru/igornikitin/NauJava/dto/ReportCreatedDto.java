package ru.igornikitin.NauJava.dto;

import ru.igornikitin.NauJava.entity.ReportStatus;

/** Ответ на запуск формирования отчёта. */
public record ReportCreatedDto(Long id, ReportStatus status) {

}
