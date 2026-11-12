package ru.igornikitin.NauJava.dto;

/**
 * Единый формат ответа об ошибке: {"message": "..."}.
 * В методичке класс назван Exception, но это имя перекрывает java.lang.Exception,
 * а ErrorResponse уже занят интерфейсом org.springframework.web.ErrorResponse.
 */
public class ErrorMessage {

	private String message;

	private ErrorMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static ErrorMessage create(Throwable e) {
		return new ErrorMessage(e.getMessage());
	}

	public static ErrorMessage create(String message) {
		return new ErrorMessage(message);
	}

}
