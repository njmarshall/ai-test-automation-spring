package com.njmarshall.aitest;

import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the framework.
 *
 * <p>This is a test-automation framework that happens to run on Spring Boot. Booting the
 * container gives us three things the Java/Python versions had to hand-build: singleton
 * lifecycle management, dependency injection for wiring, and externalized configuration.
 */
@SpringBootApplication
@EnableConfigurationProperties(FrameworkProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
