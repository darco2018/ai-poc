package com.aipoc.aipoc.documentqa;

import com.anthropic.models.messages.MessageDeltaUsage;
import com.anthropic.models.messages.RawMessageDeltaEvent;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.Usage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "document-qa", name = "mock-enabled", havingValue = "true")
public class MockDocumentQuestionService implements DocumentQuestionService {

	private static final Logger log = LoggerFactory.getLogger(MockDocumentQuestionService.class);

	private final DocumentLoader documentLoader;

	public MockDocumentQuestionService(DocumentLoader documentLoader) {
		this.documentLoader = documentLoader;
		log.info(">>> Mock DocumentQuestionService is active (document-qa.mock-enabled=true)");
	}

	@Override
	public AnswerStream answer(AnswerRequest request) {
		String file = request.fileName() != null ? request.fileName() : "Machines-of-Loving-Grace.txt";
		documentLoader.load(file);

		List<String> mockChunks = List.of(
				"Based on the provided document, ",
				"here is a mocked answer for testing and development: ",
				"Powerful AI has significant upside across biology, neuroscience, ",
				"economic development, peace, and work, ",
				"while real bottlenecks often involve physical experiments and clinical validation."
		);

		Usage mockUsage = Usage.builder()
				.inputTokens(250L)
				.outputTokens(40L)
				.cacheCreationInputTokens(0L)
				.cacheReadInputTokens(150L)
				.cacheCreation(java.util.Optional.empty())
				.inferenceGeo(java.util.Optional.empty())
				.serverToolUse(java.util.Optional.empty())
				.serviceTier(java.util.Optional.empty())
				.build();

		RawMessageDeltaEvent mockDelta = RawMessageDeltaEvent.builder()
				.delta(RawMessageDeltaEvent.Delta.builder()
						.stopReason(StopReason.END_TURN)
						.container(java.util.Optional.empty())
						.stopDetails(java.util.Optional.empty())
						.stopSequence(java.util.Optional.empty())
						.build())
				.usage(MessageDeltaUsage.builder()
						.outputTokens(40L)
						.cacheCreationInputTokens(java.util.Optional.empty())
						.cacheReadInputTokens(java.util.Optional.empty())
						.inputTokens(java.util.Optional.empty())
						.serverToolUse(java.util.Optional.empty())
						.build())
				.build();

		return consumer -> {
			AnthropicDocumentQuestionService.logUsage(mockUsage);
			for (String chunk : mockChunks) {
				consumer.accept(chunk);
			}
			AnthropicDocumentQuestionService.logCompletion(mockDelta);
		};
	}

}
