package ru.igornikitin.NauJava.dto;

import java.util.List;

/** Результат объединения двух независимых CompletableFuture через thenCombine. */
public record CombineExampleDto(long userCount, List<String> projectNames) {

}
