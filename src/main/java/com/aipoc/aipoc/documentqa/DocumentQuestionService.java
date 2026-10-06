package com.aipoc.aipoc.documentqa;

/** Answers the configured question about a document. */
public interface DocumentQuestionService {

	/**
	 * Loads the document and prepares the request. Nothing is sent until the returned
	 * stream is consumed, so a missing document fails here, before any response is written.
	 *
	 * @throws DocumentNotFoundException if the document cannot be loaded
	 */
	AnswerStream answer(AnswerRequest request);

}
