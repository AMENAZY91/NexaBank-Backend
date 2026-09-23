package com.nexabank.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta para GET /api/v1/users/me.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String displayName;
    private String phone;
    private String status;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
