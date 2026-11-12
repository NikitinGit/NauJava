package ru.igornikitin.NauJava.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Сохранённый результат формирования статистического отчёта. */
@Entity
@Table(name = "reports")
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReportStatus status = ReportStatus.CREATED;

	/**
	 * Готовая HTML-страница; до завершения формирования равна null.
	 * PostgreSQL text хранит сам HTML, а не OID большого объекта — так содержимое видно и в DataGrip.
	 */
	@Column(columnDefinition = "text")
	private String content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ReportStatus getStatus() {
		return status;
	}

	public void setStatus(ReportStatus status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
