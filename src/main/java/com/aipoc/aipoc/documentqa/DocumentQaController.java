package com.aipoc.aipoc.documentqa;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Streams Claude's answer about a document as plain text, e.g.
 * {@code curl -N "localhost:8080/api/documents/answer?file=Machines-of-Loving-Grace.txt"}.
 * Without {@code file}, the configured default document is used.
 */
@RestController
@RequestMapping(DocumentQaController.BASE_PATH)
// QA stands for question answering, the usual NLP term for a system
// that answers questions about a text.
public class DocumentQaController {

	static final String BASE_PATH = "/api/documents";
	static final String ANSWER_PATH = "/answer";
	static final String PARAM_FILE = "file";
	static final String PARAM_MODEL = "model";
	static final String PARAM_MAX_TOKENS = "maxTokens";

	private static final MediaType TEXT_PLAIN_UTF8 =
			new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

	private final DocumentQuestionService questionService;
	private final DocumentQaProperties properties;

	public DocumentQaController(DocumentQuestionService questionService, DocumentQaProperties properties) {
		this.questionService = questionService;
		this.properties = properties;
	}

	@GetMapping(ANSWER_PATH)
	public ResponseEntity<StreamingResponseBody> answer(
			@RequestParam(name = PARAM_FILE, required = false) String fileName,
			@RequestParam(name = PARAM_MODEL, required = false) String model,
			@RequestParam(name = PARAM_MAX_TOKENS, required = false) Long maxTokens) {
		AnswerRequest request = new AnswerRequest(
				fileName != null ? fileName : properties.defaultFile(),
				model != null ? model : properties.defaultModel(),
				maxTokens != null ? maxTokens : properties.defaultMaxTokens());
		AnswerStream answer = questionService.answer(request);

		StreamingResponseBody body = outputStream -> {
			Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			answer.forEachText(text -> write(writer, text));
			writer.flush();
		};
		return ResponseEntity.ok().contentType(TEXT_PLAIN_UTF8).body(body);
	}

	/** Flushes per fragment so the client sees tokens as they arrive. */
	private static void write(Writer writer, String text) {
		try {
			writer.write(text);
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
