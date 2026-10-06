package com.aipoc.aipoc.documentqa;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.TextBlockParam;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnthropicDocumentQuestionService implements DocumentQuestionService {

	private final AnthropicClient client;
	private final DocumentLoader documentLoader;
	private final DocumentQaProperties properties;

	public AnthropicDocumentQuestionService(AnthropicClient client, DocumentLoader documentLoader,
			DocumentQaProperties properties) {
		this.client = client;
		this.documentLoader = documentLoader;
		this.properties = properties;
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
				stream.stream()
						.flatMap(event -> event.contentBlockDelta().stream())
						.flatMap(deltaEvent -> deltaEvent.delta().text().stream())
						.forEach(textDelta -> consumer.accept(textDelta.text()));
			}
		};
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
