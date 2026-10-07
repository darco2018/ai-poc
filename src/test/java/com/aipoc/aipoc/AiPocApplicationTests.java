package com.aipoc.aipoc;

import static org.assertj.core.api.Assertions.assertThat;

import com.aipoc.aipoc.documentqa.Document;
import com.aipoc.aipoc.documentqa.DocumentLoader;
import com.aipoc.aipoc.documentqa.DocumentQaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiPocApplicationTests {

	@Autowired
	private DocumentQaProperties properties;

	@Autowired
	private DocumentLoader documentLoader;

	@Test
	void contextLoads() {
		assertThat(properties.defaultFile()).isEqualTo("Machines-of-Loving-Grace-introduction-1300-tokens.txt");
		Document defaultDoc = documentLoader.load(properties.defaultFile());
		assertThat(defaultDoc).isNotNull();
		assertThat(defaultDoc.content()).contains("How AI Could Transform the World for the Better");

		Document doc7000 = documentLoader.load("Machines-of-Loving-Grace-7000-tokens.txt");
		assertThat(doc7000).isNotNull();

		Document fullDoc = documentLoader.load("Machines-of-Loving-Grace.txt");
		assertThat(fullDoc).isNotNull();
	}

}
