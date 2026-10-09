package com.aipoc.aipoc.documentqa.anthropic.filedocument.service;

import java.util.function.Consumer;

/** A prepared answer that streams its text when consumed. */
@FunctionalInterface
public interface AnswerStream {

	/** Sends the request and passes each text fragment to {@code consumer} as it arrives. */
	void forEachText(Consumer<String> consumer);

}
