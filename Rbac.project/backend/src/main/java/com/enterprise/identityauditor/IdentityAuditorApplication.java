package com.enterprise.identityauditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdentityAuditorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityAuditorApplication.class, args);
    }
}
