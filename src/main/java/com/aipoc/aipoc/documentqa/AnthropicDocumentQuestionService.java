package com.aipoc.aipoc.documentqa;

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
@ConditionalOnProperty(prefix = "document-qa", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
// Spring instantiates exactly one of 2 implementations and registers one as a bean.
public class AnthropicDocumentQuestionService implements DocumentQuestionService {

	private static final Logger log = LoggerFactory.getLogger(AnthropicDocumentQuestionService.class);

	private final AnthropicClient client;
	private final DocumentLoader documentLoader;
	private final DocumentQaProperties properties;

	public AnthropicDocumentQuestionService(AnthropicClient client, DocumentLoader documentLoader,
			DocumentQaProperties properties) {
		this.client = client;
		this.documentLoader = documentLoader;
		this.properties = properties;

		log.info(">>> AnthropicDocumentQuestionService is active (document-qa.mock-enabled=false)");

	}

	@Override
	public AnswerStream answer(AnswerRequest request) {
		MessageCreateParams params = buildParams(request, documentLoader.load(request.fileName()));
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
					event.messageDelta().ifPresent(AnthropicDocumentQuestionService::logCompletion);
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
	static void logCompletion(RawMessageDeltaEvent deltaEvent) {
		log.info("Stop reason: {}, output tokens: {}",
				deltaEvent.delta().stopReason().map(Object::toString).orElse("unknown"),
				deltaEvent.usage().outputTokens());
	}

	/** Cache write > 0 on the first request, cache read > 0 on a repeat within the cache TTL. */
	static void logUsage(Usage usage) {
		log.info("Input tokens: {}, cache write(cache creation input): {}, cache read: {}",
				usage.inputTokens(),
				usage.cacheCreationInputTokens().orElse(0L),
				usage.cacheReadInputTokens().orElse(0L));
	}

	private MessageCreateParams buildParams(AnswerRequest request, Document document) {
		return MessageCreateParams.builder()
				.model(request.model())
				.maxTokens(request.maxTokens())
				.system(properties.systemPrompt())
				.addUserMessageOfBlockParams(List.of(
						ContentBlockParam.ofDocument(toDocumentBlock(document)),
						ContentBlockParam.ofText(TextBlockParam.builder().text(properties.userPrompt()).build())))
				.build();
	}

	/** Citations point into the text; cache control lets repeat questions reuse the document. */
	private static DocumentBlockParam toDocumentBlock(Document document) {
		return DocumentBlockParam.builder()
				.source(PlainTextSource.builder().data(document.content()).build())
				.title(document.title())
				.citations(CitationsConfigParam.builder().enabled(true).build())
				.cacheControl(CacheControlEphemeral.builder().build())
				.build();
	}

}
