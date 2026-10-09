package com.aipoc.aipoc.documentqa.anthropic.filedocument.service;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileAnswerRequest;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.loader.FileDocumentLoader;
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
@ConditionalOnProperty(prefix = "file-document-qa", name = "mock-enabled", havingValue = "true")
public class MockAnthropicQuestionService implements AnthropicQuestionService {

	private static final Logger log = LoggerFactory.getLogger(MockAnthropicQuestionService.class);

	private final FileDocumentLoader fileDocumentLoader;

	public MockAnthropicQuestionService(FileDocumentLoader fileDocumentLoader) {
		this.fileDocumentLoader = fileDocumentLoader;
		log.info(">>> Mock DocumentQuestionService is active (document-qa.mock-enabled=true)");
	}

	@Override
	public AnswerStream answer(FileAnswerRequest request) {
		log.info("fileName: " + request.fileName());
		String file = request.fileName() == null || request.fileName().isBlank()? "Machines-of-Loving-Grace-introduction-1300-tokens.txt" : request.fileName() ;
		fileDocumentLoader.load(file);

		List<String> mockChunks = List.of(
				"Based on the provided document, ",
				"here is a mocked answer for testing and development: "
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
			AnthropicQuestionServiceImpl.logUsage(mockUsage);
			for (String chunk : mockChunks) {
				consumer.accept(chunk);
			}
			AnthropicQuestionServiceImpl.logCompletion(mockDelta);
		};
	}

}
