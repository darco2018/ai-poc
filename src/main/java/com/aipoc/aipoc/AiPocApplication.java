package com.aipoc.aipoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/*
@SpringBootConfiguration   - indicator that the annotated class is the primary configuration clas
@EnableAutoConfiguration   - "convention-over-configuration", automatically configure sensile default beans and
infrastructure based on the libraries and dependencies present on your classpath (e.g., if h2 or is on the classpath,
it sets up an in-memory database), configures  beans and properties set in configuration files
@ComponentScan - eg detects @Configuration classes
@ComponentScan only looks for classes marked with @Component
(or stereotypes like @Service, @Repository, @Controller, @Configuration).
* */
public class AiPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiPocApplication.class, args);
	}

}
