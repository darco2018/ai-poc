package com.aipoc.aipoc.documentqa;

/** Loads documents by file name. */
public interface DocumentLoader {

	/**
	 * @throws DocumentNotFoundException if the name is not a readable document
	 */
	Document load(String fileName);

}
