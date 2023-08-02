package com.hust.edu.vn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@SpringBootApplication
@EnableWebSecurity
public class BeDocumentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeDocumentsApplication.class, args);
	}

}
