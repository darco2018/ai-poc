package com.aipoc.aipoc.documentqa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "document-qa.mock-enabled=true")
@AutoConfigureMockMvc
class MockDocumentQuestionServiceTest {

	@Autowired
	private DocumentQuestionService questionService;

	@Autowired
	private DocumentQaProperties properties;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void mockServiceBeanIsLoadedWhenMockEnabled() {
		assertThat(questionService).isInstanceOf(MockDocumentQuestionService.class);
		assertThat(properties.mockEnabled()).isTrue();
	}

	@Test
	void answerStreamsMockContentDirectly() {
		AnswerRequest request = new AnswerRequest(
				properties.defaultFile(),
				properties.defaultModel(),
				properties.defaultMaxTokens()
		);

		AnswerStream stream = questionService.answer(request);
		List<String> chunks = new ArrayList<>();
		stream.forEachText(chunks::add);

		assertThat(chunks).isNotEmpty();
		String fullAnswer = String.join("", chunks);
		assertThat(fullAnswer).contains("Based on the provided document");
		assertThat(fullAnswer).contains("Powerful AI has significant upside across biology");
	}

	@Test
	void answerThrowsWhenDocumentNotFound() {
		AnswerRequest request = new AnswerRequest(
				"non-existent-file.txt",
				properties.defaultModel(),
				properties.defaultMaxTokens()
		);

		assertThatThrownBy(() -> questionService.answer(request))
				.isInstanceOf(DocumentNotFoundException.class);
	}

	@Test
	void getAnswerEndpointStreamsMockOutput() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/documents/answer"))
				.andExpect(request().asyncStarted())
				.andReturn();

		mockMvc.perform(asyncDispatch(result))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Based on the provided document")));
	}

	@Test
	void getAnswerEndpointReturnsNotFoundForMissingDocument() throws Exception {
		mockMvc.perform(get("/api/documents/answer").param("file", "missing-doc.txt"))
				.andExpect(status().isNotFound());
	}

}
