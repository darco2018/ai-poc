package com.aipoc.aipoc.textqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "text-qa")
public record TextQaProperties (
        Integer defaultMaxTokens,
        String defaultRole,
        boolean mockEnabled)
{}
