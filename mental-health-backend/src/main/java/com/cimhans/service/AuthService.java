package com.cimhans.service;

import com.cimhans.domain.entity.RefreshToken;
import com.cimhans.domain.entity.User;
import com.cimhans.domain.enums.NotificationType;
import com.cimhans.domain.enums.Role;
import com.cimhans.dto.request.LoginRequest;
import com.cimhans.dto.request.RegisterRequest;
import com.cimhans.dto.response.AuthResponse;
import com.cimhans.exception.BusinessException;
import com.cimhans.exception.ResourceNotFoundException;
import com.cimhans.exception.UnauthorizedException;
import com.cimhans.repository.RefreshTokenRepository;
import com.cimhans.repository.UserRepository;
import com.cimhans.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = jwtTokenProvider.generateRefreshTokenValue();
        String refreshTokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiry * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);

        auditService.logSuccess("USER_LOGIN", "User", user.getId(), "Login from " + user.getEmail());
        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, rawRefreshToken);
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        auditService.logSuccess("USER_REGISTERED", "User", user.getId(), user.getEmail());

        notificationService.createNotification(user, NotificationType.WELCOME,
                "Welcome to CIMHANS",
                "Your account has been created. Welcome, " + user.getFirstName() + "!",
                user.getId(), "User");

        return user;
    }

    /**
     * Google OAuth flow: frontend sends the access_token, we verify it via
     * Google's userinfo endpoint, then find-or-create the user and issue JWT.
     */
    @Transactional
    public AuthResponse googleLogin(String accessToken) {
        // Verify token and fetch profile from Google
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";
        @SuppressWarnings("unchecked")
        Map<String, Object> profile;
        try {
            profile = (Map<String, Object>) restTemplate.getForObject(
                url + "?access_token=" + accessToken, Map.class);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Google token");
        }
        if (profile == null || profile.get("email") == null) {
            throw new UnauthorizedException("Could not retrieve Google profile");
        }

        String email      = (String) profile.get("email");
        String firstName  = (String) profile.getOrDefault("given_name",  "Google");
        String lastName   = (String) profile.getOrDefault("family_name", "User");
        boolean verified  = Boolean.TRUE.equals(profile.get("email_verified"));

        // Find existing user or create new PATIENT account
        Optional<User> existing = userRepository.findByEmailAndDeletedAtIsNull(email);
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            if (!user.isActive()) throw new UnauthorizedException("Account deactivated. Contact administrator.");
        } else {
            user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.PATIENT)
                    .isActive(true)
                    .isEmailVerified(verified)
                    .build();
            user = userRepository.saveAndFlush(user);
            log.info("New user created via Google OAuth: {}", email);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String jwtAccessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = jwtTokenProvider.generateRefreshTokenValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiry * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("Google OAuth login: {}", email);
        return buildAuthResponse(user, jwtAccessToken, rawRefreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (!stored.isValid()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new UnauthorizedException("Refresh token expired. Please login again.");
        }

        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRawRefreshToken = jwtTokenProvider.generateRefreshTokenValue();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRawRefreshToken))
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiry * 1_000_000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRawRefreshToken);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
        auditService.logSuccess("USER_LOGOUT", "User", userId, "All tokens revoked");
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String rawRefreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry() / 1000)
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .build())
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }
}
