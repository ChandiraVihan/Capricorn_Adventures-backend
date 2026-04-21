package com.capricorn_adventures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CapricornAdventuresApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapricornAdventuresApplication.class, args);
    }

}
