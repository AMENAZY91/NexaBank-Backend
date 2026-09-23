package com.nexabank.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Entidad de dominio — documento Firestore {@code users}.
 *
 * <p>Esquema:
 * <pre>
 * {
 *   "id": "USR-000001",
 *   "email": "user@example.com",
 *   "displayName": "Usuario Nexa",
 *   "phone": "+573000000000",
 *   "status": "ACTIVE",
 *   "roles": ["USER"],
 *   "createdAt": "Timestamp",
 *   "updatedAt": "Timestamp",
 *   "lastLoginAt": "Timestamp"
 * }
 * </pre>
 *
 * <p>No almacenar contraseñas en Firestore si se utiliza Firebase Authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String email;
    private String displayName;
    private String phone;
    private String status;

    @Builder.Default
    private List<String> roles = List.of("USER");

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
