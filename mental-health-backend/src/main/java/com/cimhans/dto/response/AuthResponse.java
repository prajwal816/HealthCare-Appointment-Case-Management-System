package com.cimhans.dto.response;

import com.cimhans.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private String profilePictureUrl;
    }
}
