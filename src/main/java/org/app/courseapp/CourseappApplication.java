package org.app.courseapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CourseappApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseappApplication.class, args);
    }

}
