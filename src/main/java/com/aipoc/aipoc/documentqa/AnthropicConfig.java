package com.aipoc.aipoc.documentqa;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocumentQaProperties.class)
public class AnthropicConfig {

	/** Reads ANTHROPIC_API_KEY from the environment; closed by Spring on shutdown. */
	@Bean
	public AnthropicClient anthropicClient() {
		return AnthropicOkHttpClient.fromEnv();
	}

}
