package org.example.stremioaddon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StremioAddonApplication {

	public static void main(String[] args) {
		SpringApplication.run(StremioAddonApplication.class, args);
	}

}
