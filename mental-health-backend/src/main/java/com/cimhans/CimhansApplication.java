package com.cimhans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the CIMHANS Mental Health Appointment & Case Management System.
 * Enables JPA auditing for automatic createdAt/updatedAt population,
 * caching via Redis, async processing for notifications, and scheduling for reminders.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableCaching
@EnableAsync
@EnableScheduling
public class CimhansApplication {

    public static void main(String[] args) {
        SpringApplication.run(CimhansApplication.class, args);
    }
}
