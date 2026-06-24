package com.healthcare.healthcare_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthcarePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcarePlatformApplication.class, args);
	}
}