package ru.igornikitin.NauJava.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.igornikitin.NauJava.dto.ErrorMessage;

/**
 * Единая обработка ошибок: любая ошибка возвращается как {"message": "..."} с подходящим HTTP-статусом.
 * Наивысший приоритет нужен, чтобы этот обработчик срабатывал раньше встроенного обработчика
 * Spring Data REST (RepositoryRestExceptionHandler), у которого свой формат ответа.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionControllerAdvice {

	private static final Logger log = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

	/** Запись не найдена, например GET /tasks/999999. */
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorMessage exception(ResourceNotFoundException e) {
		return ErrorMessage.create("Запись не найдена");
	}

	/** Некорректные данные запроса: неверный тип параметра или id в пути, нечитаемый JSON, неверное значение. */
	@ExceptionHandler({ TypeMismatchException.class, ConversionException.class, HttpMessageNotReadableException.class,
			IllegalArgumentException.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessage badRequest(Exception e) {
		return ErrorMessage.create(e);
	}

	/** Нарушение ограничений БД: удаление записи, на которую ссылаются другие, дубль уникального поля. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorMessage conflict(DataIntegrityViolationException e) {
		return ErrorMessage.create("Операция нарушает целостность данных: есть связанные записи или такое значение уже существует");
	}

	/**
	 * Все остальные ошибки. Стандартные ошибки Spring MVC (нет параметра, неизвестный путь, неверный HTTP-метод)
	 * сами знают свой статус через интерфейс ErrorResponse; для прочих — 500.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> exception(Exception e) {
		HttpStatusCode status = e instanceof ErrorResponse errorResponse ? errorResponse.getStatusCode()
				: HttpStatus.INTERNAL_SERVER_ERROR;
		if (status.is5xxServerError()) {
			// обработанное исключение Spring сам не логирует — без этого стектрейс потерялся бы
			log.error("Необработанная ошибка", e);
		}
		return ResponseEntity.status(status).body(ErrorMessage.create(e));
	}

}
