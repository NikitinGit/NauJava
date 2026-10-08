package ru.igornikitin.NauJava.repository;

import java.util.List;

public interface CrudRepository<T, ID> {

	/** В Spring Data (org.springframework.data.repository.CrudRepository) вместо create и update — один метод save. */
	void create(T entity);

	T read(ID id);

	List<T> readAll();

	/** В Spring Data — тоже save: при наличии id выполняется обновление. */
	void update(T entity);

	void delete(ID id);

}
