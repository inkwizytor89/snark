package org.enoch.snark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnarkApplication.class, args);
	}

}
