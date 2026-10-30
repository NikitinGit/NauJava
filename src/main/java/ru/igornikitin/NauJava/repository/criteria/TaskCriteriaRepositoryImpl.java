package ru.igornikitin.NauJava.repository.criteria;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.igornikitin.NauJava.entity.Task;
import ru.igornikitin.NauJava.entity.TaskStatus;
import ru.igornikitin.NauJava.entity.User;

// Не называть TaskRepositoryImpl: Spring Data по соглашению подмешал бы такой класс в TaskRepository
@Repository
public class TaskCriteriaRepositoryImpl implements TaskCriteriaRepository {

	private final EntityManager entityManager;

	public TaskCriteriaRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Task> findByStatusAndDueDateBetween(TaskStatus status, LocalDate from, LocalDate to) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);

		Root<Task> taskRoot = criteriaQuery.from(Task.class);
		Predicate statusPredicate = criteriaBuilder.equal(taskRoot.get("status"), status);
		Predicate dueDatePredicate = criteriaBuilder.between(taskRoot.get("dueDate"), from, to);

		criteriaQuery.select(taskRoot).where(criteriaBuilder.and(statusPredicate, dueDatePredicate));

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<Task> findByAssigneeLogin(String login) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);

		Root<Task> taskRoot = criteriaQuery.from(Task.class);
		Join<Task, User> assignee = taskRoot.join("assignee", JoinType.INNER);
		Predicate loginPredicate = criteriaBuilder.equal(assignee.get("login"), login);

		criteriaQuery.select(taskRoot).where(loginPredicate);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

}
