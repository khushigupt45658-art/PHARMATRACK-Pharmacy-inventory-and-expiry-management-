package com.pharmatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PharmaTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaTrackApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  PharmaTrack Web Application Started Successfully");
        System.out.println("  Access UI: http://localhost:8080");
        System.out.println("  API Docs:  http://localhost:8080/api");
        System.out.println("=================================================");
    }
}
