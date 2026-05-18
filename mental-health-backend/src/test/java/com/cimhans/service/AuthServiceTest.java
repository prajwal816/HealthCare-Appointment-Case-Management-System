package com.cimhans.service;

import com.cimhans.domain.entity.User;
import com.cimhans.domain.enums.Role;
import com.cimhans.dto.request.LoginRequest;
import com.cimhans.dto.response.AuthResponse;
import com.cimhans.exception.BusinessException;
import com.cimhans.repository.RefreshTokenRepository;
import com.cimhans.repository.UserRepository;
import com.cimhans.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock AuthenticationManager authenticationManager;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditService auditService;
    @Mock NotificationService notificationService;

    @InjectMocks
    AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiry", 604800000L);
        mockUser = User.builder()
                .email("test@cimhans.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.ADMIN)
                .passwordHash("$2a$12$hashed")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Login should return AuthResponse with tokens on valid credentials")
    void login_validCredentials_returnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@cimhans.com");
        request.setPassword("Test@123");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(mockUser, null));
        when(userRepository.findByEmailAndDeletedAtIsNull("test@cimhans.com"))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtTokenProvider.generateAccessToken(mockUser)).thenReturn("mock.access.token");
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn("mock-refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiry()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@cimhans.com");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(auditService).logSuccess(eq("USER_LOGIN"), eq("User"), any(), any());
    }

    @Test
    @DisplayName("Login should throw when authentication fails")
    void login_invalidCredentials_throwsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@cimhans.com");
        request.setPassword("WrongPass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Logout should revoke all refresh tokens for the user")
    void logout_revokesAllUserTokens() {
        UUID userId = mockUser.getId();
        authService.logout(userId);
        verify(refreshTokenRepository).revokeAllForUser(userId);
        verify(auditService).logSuccess(eq("USER_LOGOUT"), eq("User"), eq(userId), any());
    }
}
