package com.aipoc.aipoc.documentqa.anthropic.filedocument.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileDocument;
import com.aipoc.aipoc.documentqa.anthropic.filedocument.config.FileDocumentQaProperties;
import org.springframework.stereotype.Component;

/**
 * Reads {@code .txt} documents from the configured documents directory.
 * <p>
 * The file name comes from an HTTP request, so only plain file names directly inside
 * the directory are accepted - no sub-paths, no {@code ..}, no other extensions.
 */
@Component
public class FileSystemDocumentLoader implements FileDocumentLoader {

	private static final String TEXT_EXTENSION = ".txt";
	private static final char TITLE_WORD_SEPARATOR = '-';

	private final Path documentsDir;

	public FileSystemDocumentLoader(FileDocumentQaProperties properties) {
		this.documentsDir = Path.of(properties.documentsDir()).toAbsolutePath().normalize();
	}

	@Override
	public FileDocument load(String fileName) {
		Path file = resolve(fileName);
		try {
			return new FileDocument(toTitle(fileName), Files.readString(file));
		} catch (IOException e) {
			throw new DocumentNotFoundException(fileName, e);
		}
	}

	private Path resolve(String fileName) {
		if (fileName == null || !fileName.endsWith(TEXT_EXTENSION)) {
			throw new DocumentNotFoundException(fileName);
		}
		Path file = documentsDir.resolve(fileName).normalize();
		if (!documentsDir.equals(file.getParent()) || !Files.isRegularFile(file)) {
			throw new DocumentNotFoundException(fileName);
		}
		return file;
	}

	/** "Machines-of-Loving-Grace.txt" -> "Machines of Loving Grace" */
	private static String toTitle(String fileName) {
		return fileName.substring(0, fileName.length() - TEXT_EXTENSION.length())
				.replace(TITLE_WORD_SEPARATOR, ' ');
	}

}
