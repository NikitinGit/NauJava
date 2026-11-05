package ru.igornikitin.NauJava.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.igornikitin.NauJava.dto.CombineExampleDto;
import ru.igornikitin.NauJava.dto.CombineErrorExampleDto;
import ru.igornikitin.NauJava.dto.ComposeExampleDto;
import ru.igornikitin.NauJava.service.CompletableFutureExampleService;

/** HTTP-точки для ручного запуска учебных примеров CompletableFuture. */
@RestController
@RequestMapping("/examples/completable-futures")
public class CompletableFutureExampleController {

	private final CompletableFutureExampleService exampleService;

	public CompletableFutureExampleController(CompletableFutureExampleService exampleService) {
		this.exampleService = exampleService;
	}

	/** GET /examples/completable-futures/combine */
	@GetMapping("/combine")
	public CompletableFuture<CombineExampleDto> thenCombine() {
		return exampleService.thenCombineExample();
	}

	/** GET /examples/completable-futures/combine-error */
	@GetMapping("/combine-error")
	public CompletableFuture<CombineErrorExampleDto> thenCombineWithError() {
		return exampleService.thenCombineWithErrorExample();
	}

	/** GET /examples/completable-futures/compose/138 */
	@GetMapping("/compose/{projectId}")
	public CompletableFuture<ComposeExampleDto> thenCompose(@PathVariable Long projectId) {
		return exampleService.thenComposeExample(projectId);
	}

}
