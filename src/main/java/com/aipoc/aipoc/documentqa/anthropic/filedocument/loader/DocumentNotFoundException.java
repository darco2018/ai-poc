package com.aipoc.aipoc.documentqa.anthropic.filedocument.loader;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends RuntimeException {

	private static final String MESSAGE_PREFIX = "Document not found: ";

	public DocumentNotFoundException(String fileName) {
		super(MESSAGE_PREFIX + fileName);
	}

	public DocumentNotFoundException(String fileName, Throwable cause) {
		super(MESSAGE_PREFIX + fileName, cause);
	}

}
