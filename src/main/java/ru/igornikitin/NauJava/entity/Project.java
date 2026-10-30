package ru.igornikitin.NauJava.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private LocalDate deadline;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	/** Обратная сторона связи: внешний ключ project_id лежит в таблице tasks. */
	@OneToMany(mappedBy = "project")
	private List<Task> tasks = new ArrayList<>();

	/** Обратная сторона связи: внешний ключ project_id лежит в таблице project_members. */
	@OneToMany(mappedBy = "project")
	private List<ProjectMember> members = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public List<ProjectMember> getMembers() {
		return members;
	}

	/** Связанные сущности (owner, tasks, members) не выводятся: они загружаются лениво. */
	@Override
	public String toString() {
		return "Project{id=" + id
				+ ", name='" + name + '\''
				+ ", description=" + (description == null ? "—" : "'" + description + "'")
				+ ", deadline=" + deadline + '}';
	}

}
