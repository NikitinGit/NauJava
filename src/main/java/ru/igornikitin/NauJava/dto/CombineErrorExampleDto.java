package ru.igornikitin.NauJava.dto;

/** Наблюдаемый результат thenCombine, когда первая операция завершается ошибкой. */
public record CombineErrorExampleDto(String exceptionType, String message, boolean secondTaskFinished,
		long elapsedMillis) {

}
