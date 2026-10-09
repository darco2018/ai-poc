package com.aipoc.aipoc.documentqa;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Streams Claude's answer about a document as plain text, e.g.
 * {@code curl -N "localhost:8080/api/documents/answer?file=Machines-of-Loving-Grace.txt"}.
 * Without {@code file}, the configured default document is used.
 */
@RestController
// >> it is a spring-controlled bean
// @Controller: Marks the class as a Spring MVC component/controller discovered via component scanning.
// plus @ResponseBody: Indicates that the return value of every handler method should be bound directly
// //to the web response body (via HttpMessageConverters), rather than interpreted as a view name.

@RequestMapping(DocumentQaController.BASE_PATH)
// QA stands for question answering, the usual NLP term for a system
// that answers questions about a text.
public class DocumentQaController {

	static final String BASE_PATH = "/api/documents";
	static final String ANSWER_PATH = "/answer";

	private static final MediaType TEXT_PLAIN_UTF8 =
			new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

	private final DocumentQuestionService questionService;
	private final DocumentQaProperties properties;

	// Since Spring 4.3, you don't even need to write @Autowired on a single constructor—Spring assumes it by default.
	// When Spring starts up and instantiates DocumentQaController, it looks in
	// its ApplicationContext (bean container) for matching beans for each paramete
	public DocumentQaController(DocumentQuestionService questionService, DocumentQaProperties properties) {
		this.questionService = questionService;
		this.properties = properties;
	}

	@PostMapping(ANSWER_PATH)
	public ResponseEntity<StreamingResponseBody> answer(
			@Valid @RequestBody AnswerRequest request) {
		AnswerRequest resolvedRequest = new AnswerRequest(
				request.question(),
				request.fileName() != null ? request.fileName() : properties.defaultFile(),
				request.model() != null ? request.model() : properties.defaultModel(),
				request.maxTokens() != null ? request.maxTokens() : properties.defaultMaxTokens());
		AnswerStream answer = questionService.answer(resolvedRequest);

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
