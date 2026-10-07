package com.aipoc.aipoc.documentqa;

/** What to ask: which document, with which model, and how many
 * output tokens at most.
 * Why AnswerRequest?
 * It's a request for an answer
 *
 * t holds everything the service needs to produce one: which file,
 * which model, and how many tokens. The name pairs with what comes
 * back, so answer(AnswerRequest) returns an AnswerStream.
 * That reads as "request in, answer out".
 * One oddity: it doesn't contain the question. The question is the fixed
 * document-qa.user-prompt in application.properties*/
public record AnswerRequest(String fileName, String model, long maxTokens) {
}
