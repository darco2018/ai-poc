package com.aipoc.aipoc.textqa.entity;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @NotBlank(message = "Provider is mandatory")
        String provider,
        @NotBlank(message = "Model is mandatory")
        String model,
        @NotBlank(message = "Prompt is mandatory")
        String prompt,
        @NotBlank(message = "Content is mandatory")
        String content,
        Integer maxTokens,
        String role
) {}
