package com.cimhans.security;

import com.cimhans.domain.entity.User;
import com.cimhans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Loads user by UUID (used by JwtAuthenticationFilter) or by email (used by login flow).
 * The 'username' field in this context is the UUID string from the JWT subject.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        User user;

        // JwtAuthenticationFilter passes UUID; login flow passes email
        try {
            UUID uuid = UUID.fromString(userIdOrEmail);
            user = userRepository.findByIdAndDeletedAtIsNull(uuid)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userIdOrEmail));
        } catch (IllegalArgumentException e) {
            // Not a UUID — try email
            user = userRepository.findByEmailAndDeletedAtIsNull(userIdOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userIdOrEmail));
        }

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is deactivated: " + userIdOrEmail);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}
