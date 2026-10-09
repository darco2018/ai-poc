package com.aipoc.aipoc.documentqa.anthropic.filedocument.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * How @EnableConfigurationProperties(DocumentQaProperties.class) gets activated:
 * The @ComponentScan on your main class detects AnthropicConfig (since it's a @Configuration).
 * When Spring processes AnthropicConfig, it sees @EnableConfigurationProperties(DocumentQaProperties.class)
 * and creates the DocumentQaProperties bean.*/


// You explicitly register the specific class on any @Configuration class:

@EnableConfigurationProperties(FileDocumentQaProperties.class) // instead of broad classpath scanning,with
// @ConfigurationPropertiesSca
// you explicitly register individual properties classes on a @Configuration class.
// it creates the DocumentQaProperties bean, and injects it wherever requested.
// @Configuration <<<registration << @ConfigurationProperties

@Configuration
public class FileAnthropicConfig {

	/** Reads ANTHROPIC_API_KEY from the environment; closed by Spring on shutdown. */
	@Bean
	@ConditionalOnProperty(prefix = "document-qa", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
	//  If document-qa.mock-enabled is completely absent from configuration files or environment variables, the condition evaluates to true by default
	public AnthropicClient anthropicClient() {

		/*Constructs an HTTP client configured with settings and authentication tokens
		 (such as ANTHROPIC_API_KEY) loaded directly from the system environment.*/
		return AnthropicOkHttpClient.fromEnv();
	}

	/* Properties resolution -simplified:
	* -command line
	* -OS variables
	* -profile specific
	* -application.properties
	* -default
	* */

}
