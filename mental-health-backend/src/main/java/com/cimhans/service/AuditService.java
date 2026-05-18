package com.cimhans.service;

import com.cimhans.domain.entity.AuditLog;
import com.cimhans.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Async audit logging service — fires-and-forgets to never slow down the main request.
 * Records every significant action: note access, appointment changes, user logins, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entityType, UUID entityId, String details, String outcome) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID actorId = null;
            String actorEmail = "SYSTEM";

            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                try {
                    actorId = UUID.fromString(auth.getName());
                } catch (IllegalArgumentException ignored) {}
                actorEmail = auth.getName();
            }

            AuditLog auditLog = AuditLog.builder()
                    .actorId(actorId)
                    .actorEmail(actorEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .outcome(outcome)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for action {}: {}", action, e.getMessage());
        }
    }

    @Async
    public void logSuccess(String action, String entityType, UUID entityId, String details) {
        log(action, entityType, entityId, details, "SUCCESS");
    }

    @Async
    public void logFailure(String action, String entityType, UUID entityId, String details) {
        log(action, entityType, entityId, details, "FAILURE");
    }
}
