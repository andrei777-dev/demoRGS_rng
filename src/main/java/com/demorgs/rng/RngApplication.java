package com.demorgs.rng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the RNG microservice.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RngApplication {

	public static void main(String[] args) {
		SpringApplication.run(RngApplication.class, args);
	}

}
