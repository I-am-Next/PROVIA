package com.pm.servicecatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ServiceCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCatalogApplication.class, args);
    }

}
