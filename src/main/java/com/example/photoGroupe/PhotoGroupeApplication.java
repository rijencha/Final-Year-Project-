package com.example.photoGroupe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PhotoGroupeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoGroupeApplication.class, args);
	}

}
