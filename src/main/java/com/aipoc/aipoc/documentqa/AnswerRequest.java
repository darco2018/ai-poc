package com.aipoc.aipoc.documentqa;

/** What to ask: which document, with which model, and how many output tokens at most. */
public record AnswerRequest(String fileName, String model, long maxTokens) {
}
