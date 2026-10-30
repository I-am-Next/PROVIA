package com.pm.mediastorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class MediaStorageApplication {

	public static void main(String[] args) {

		SpringApplication.run(MediaStorageApplication.class, args);
	}

}
