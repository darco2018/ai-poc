package com.aipoc.aipoc.textqa.controller;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileAnswerRequest;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.service.AnswerStream;
import com.aipoc.aipoc.textqa.config.TextQaProperties;
import com.aipoc.aipoc.textqa.entity.AnswerRequest;
import com.aipoc.aipoc.textqa.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/text")
public class TextQaController {

    private final QuestionService questionService;
    private final TextQaProperties properties;

    public TextQaController(QuestionService questionService, TextQaProperties properties) {
        this.questionService = questionService;
        this.properties = properties;
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> answer(@Valid @RequestBody AnswerRequest request) {
        AnswerRequest resolvedRequest = new AnswerRequest(
            request.provider(),
            request.model(),
            request.prompt(),
            request.content(),
            request.maxTokens() != null ? request.maxTokens() : properties.defaultMaxTokens(),
            request.role()!= null ? request.role() : properties.defaultRole());

        Map<String, Object> answer = questionService.answer(resolvedRequest);

        return ResponseEntity.ok(answer);
    }
}
