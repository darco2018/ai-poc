package com.aipoc.aipoc.documentqa.anthropic.filedocument.entity;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for asking a question about a document.
 */
public record FileAnswerRequest(
		@NotBlank(message = "Question is mandatory")
		String question,
		String fileName,
		String model,
		Long maxTokens) {
}
