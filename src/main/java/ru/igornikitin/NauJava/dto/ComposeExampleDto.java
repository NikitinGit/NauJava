package ru.igornikitin.NauJava.dto;

import java.util.List;

/** Результат цепочки CompletableFuture через thenCompose. */
public record ComposeExampleDto(Long projectId, String projectName, List<String> taskTitles) {

}
