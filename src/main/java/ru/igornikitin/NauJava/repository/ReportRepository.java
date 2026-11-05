package ru.igornikitin.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ru.igornikitin.NauJava.entity.Report;

/** Репозиторий не экспортируется: жизненным циклом отчёта управляет ReportService. */
@RepositoryRestResource(exported = false)
public interface ReportRepository extends CrudRepository<Report, Long> {

}
