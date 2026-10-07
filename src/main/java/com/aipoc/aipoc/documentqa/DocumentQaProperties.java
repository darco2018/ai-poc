package com.aipoc.aipoc.documentqa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for document question answering, bound from {@code document-qa.*} in application.properties.
 *
 * @param systemPrompt     instructions sent as the system prompt
 * @param userPrompt       question asked about the document
 * @param defaultModel     model used when the request does not specify one
 * @param defaultMaxTokens output token limit used when the request does not specify one
 * @param documentsDir     directory the documents are read from
 * @param defaultFile      document used when the request does not specify one
 * @param mockEnabled      whether mock document question service is enabled
 */
@ConfigurationProperties(prefix = "document-qa")
public record DocumentQaProperties(
		String systemPrompt,
		String userPrompt,
		String defaultModel,
		long defaultMaxTokens,
		String documentsDir,
		String defaultFile,
		boolean mockEnabled) {
}
