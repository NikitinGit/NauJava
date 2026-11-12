package ru.igornikitin.NauJava.service;

import java.util.concurrent.CompletableFuture;

import ru.igornikitin.NauJava.entity.Report;

/** Создание, асинхронное формирование и получение статистических отчётов. */
public interface ReportService {

	/** Создаёт отчёт со статусом CREATED и возвращает его идентификатор. */
	Long createReport();

	/** Запускает фоновое формирование отчёта и сразу возвращает Future. */
	CompletableFuture<Void> generateReportAsync(Long reportId);

	Report findById(Long reportId);

	/** Возвращает HTML только для успешно сформированного отчёта. */
	String getContent(Long reportId);

}
