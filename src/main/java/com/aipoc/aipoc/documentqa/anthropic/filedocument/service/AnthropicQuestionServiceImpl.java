package com.aipoc.aipoc.documentqa.anthropic.filedocument.service;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileAnswerRequest;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileDocument;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.loader.FileDocumentLoader;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.config.FileDocumentQaProperties;
import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.RawMessageDeltaEvent;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "file-document-qa", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
// Spring instantiates exactly one of 2 implementations and registers one as a bean.
public class AnthropicQuestionServiceImpl implements AnthropicQuestionService {

	private static final Logger log = LoggerFactory.getLogger(AnthropicQuestionServiceImpl.class);

	private final AnthropicClient client;
	private final FileDocumentLoader fileDocumentLoader;
	private final FileDocumentQaProperties properties;

	public AnthropicQuestionServiceImpl(AnthropicClient client, FileDocumentLoader fileDocumentLoader,
										FileDocumentQaProperties properties) {
		this.client = client;
		this.fileDocumentLoader = fileDocumentLoader;
		this.properties = properties;

		log.info(">>> AnthropicDocumentQuestionService is active (document-qa.mock-enabled=false)");

	}

	@Override
	public AnswerStream answer(FileAnswerRequest request) {
		String file = request.fileName() == null || request.fileName().isBlank()? properties.defaultFile() : request.fileName() ;
		log.info("Default file: " + file);
		MessageCreateParams params = buildParams(request, fileDocumentLoader.load(file));
		/*		.
		return consumer -> { try ... } constructs and returns an instance of AnswerStream
		where the body { try ... } serves as the exact runtime implementation of
		the single method void forEachText(Consumer<String> consumer)
		Returning a lambda means returning a deferred action (executable behavior) rather than
		static, precomputed data.
		The Java compiler maps: The parameter consumer directly to forEachText(Consumer<String> consumer)'s
		parameter.
		The return type void directly to forEachText's void return type.
		The lambda is functionally identical to instantiating an anonymous class that implements AnswerStream:

		return new AnswerStream() {
			@Override
			public void forEachText(Consumer<String> consumer) {
				try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
					stream.stream()
							.flatMap(event -> event.contentBlockDelta().stream())
							.flatMap(deltaEvent -> deltaEvent.delta().text().stream())
							.forEach(textDelta -> consumer.accept(textDelta.text()));
				}
			}
};
		*/
		return consumer -> {
			try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
				stream.stream().forEach(event -> {
					event.messageStart().ifPresent(start -> logUsage(start.message().usage()));
					event.contentBlockDelta()
							.flatMap(deltaEvent -> deltaEvent.delta().text())
							.ifPresent(textDelta -> consumer.accept(textDelta.text()));
					event.messageDelta().ifPresent(AnthropicQuestionServiceImpl::logCompletion);
				});
			}
		};
	}

	/** Stop reason "max_tokens" means the answer was cut off by the output limit
	 * The common stop reasons:
	 * end_turn: Claude finished its answer naturally.
	 * max_tokens: the answer hit your default-max-tokens limit and was cut off. Expect this often with your current setting of 200.
	 * refusal: Claude declined the request (rare).
	 * stop_sequence and tool_use: don't occur in this app, because it sets no stop sequences and defines no tool. */
	public static void logCompletion(RawMessageDeltaEvent deltaEvent) {
		log.info("Stop reason: {}, output tokens: {}",
				deltaEvent.delta().stopReason().map(Object::toString).orElse("unknown"),
				deltaEvent.usage().outputTokens());
	}

	/** Cache write > 0 on the first request, cache read > 0 on a repeat within the cache TTL. */
	public static void logUsage(Usage usage) {
		log.info("Input tokens: {}, cache write(cache creation input): {}, cache read: {}",
				usage.inputTokens(),
				usage.cacheCreationInputTokens().orElse(0L),
				usage.cacheReadInputTokens().orElse(0L));
	}

	private MessageCreateParams buildParams(FileAnswerRequest request, FileDocument fileDocument) {
		return MessageCreateParams.builder()
				.model(request.model() != null ? request.model() : properties.defaultModel())
				.maxTokens(request.maxTokens() != null ? request.maxTokens() : properties.defaultMaxTokens())
				.system(properties.systemPrompt())
				.addUserMessageOfBlockParams(List.of(
						ContentBlockParam.ofDocument(toDocumentBlock(fileDocument)),
						ContentBlockParam.ofText(TextBlockParam.builder().text(request.question()).build())))
				.build();
	}

	/** Citations point into the text; cache control lets repeat questions reuse the document. */
	private static DocumentBlockParam toDocumentBlock(FileDocument fileDocument) {
		return DocumentBlockParam.builder()
				.source(PlainTextSource.builder().data(fileDocument.content()).build())
				.title(fileDocument.title())
				.citations(CitationsConfigParam.builder().enabled(true).build())
				.cacheControl(CacheControlEphemeral.builder().build())
				.build();
	}

}
