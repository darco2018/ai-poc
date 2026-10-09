package com.aipoc.aipoc.documentqa;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for asking a question about a document.
 */
public record AnswerRequest(
		@NotBlank(message = "Question is mandatory")
		String question,
		String fileName,
		String model,
		Long maxTokens) {
}
