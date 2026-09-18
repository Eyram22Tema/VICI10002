package com.example.jibbleattendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JibbleAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JibbleAttendanceApplication.class, args);
    }
}
