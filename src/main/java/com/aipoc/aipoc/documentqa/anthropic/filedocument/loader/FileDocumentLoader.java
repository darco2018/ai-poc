package com.aipoc.aipoc.documentqa.anthropic.filedocument.loader;

import com.aipoc.aipoc.documentqa.anthropic.filedocument.entity.FileDocument;

/** Loads documents by file name. */
public interface FileDocumentLoader {

	/**
	 * @throws DocumentNotFoundException if the name is not a readable document
	 */
	FileDocument load(String fileName);

}
