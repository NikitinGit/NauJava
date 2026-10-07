package ru.igornikitin.NauJava.entity;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class Project {

	private Long id;

	private String name;

	private String description;

	private LocalDate deadline;

	private Set<String> members = new LinkedHashSet<>();

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

	public Set<String> getMembers() {
		return members;
	}

	public void setMembers(Set<String> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return "Project{id=" + id
				+ ", name='" + name + '\''
				+ ", description='" + description + '\''
				+ ", deadline=" + deadline
				+ ", members=" + members + '}';
	}

}
