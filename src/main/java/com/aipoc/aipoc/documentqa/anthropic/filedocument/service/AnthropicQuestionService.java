package com.aipoc.aipoc.documentqa.anthropic.filedocument.service;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileAnswerRequest;

/** Answers the configured question about a document. */
public interface AnthropicQuestionService {

	AnswerStream answer(FileAnswerRequest request);

}
