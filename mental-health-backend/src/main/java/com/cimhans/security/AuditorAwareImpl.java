package com.cimhans.security;

import com.cimhans.domain.entity.User;
import com.cimhans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the current authenticated user's email for JPA auditing (createdBy, updatedBy fields).
 */
@Component("auditorAwareImpl")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final UserRepository userRepository;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        try {
            UUID userId = UUID.fromString(authentication.getName());
            return userRepository.findByIdAndDeletedAtIsNull(userId)
                    .map(User::getEmail);
        } catch (Exception e) {
            return Optional.of(authentication.getName());
        }
    }
}
