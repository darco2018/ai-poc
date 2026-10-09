package com.aipoc.aipoc.documentqa.anthropic.filedocument.config;

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

// Spring Boot scans for configuration properties (either via @ConfigurationPropertiesScan or @EnableConfigurationProperties).
/*@ConfigurationProperties by itself does not register a Spring bean. It is simply metadata that tells Spring: "If
this class is ever turned into a bean, bind its fields from application.properties using the prefix 'document-qa'."*/

	// @ConfigurationProperties is not a @Component, so @ComponentScan doesn't find it
@ConfigurationProperties(prefix = "file-document-qa")
public record FileDocumentQaProperties(
		String systemPrompt,
		String userPrompt,
		String defaultModel,
		long defaultMaxTokens,
		String documentsDir,
		String defaultFile,
		boolean mockEnabled) {
}
