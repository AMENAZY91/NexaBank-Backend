package com.nexabank.api.repository;

import com.google.cloud.firestore.*;
import com.nexabank.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Repository para la colección {@code users} de Firestore.
 */
@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private static final String COLLECTION = "users";

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<User> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) {
                return Optional.of(documentToUser(doc));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding user by id: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot query = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get().get();

            if (!query.isEmpty()) {
                return Optional.of(documentToUser(query.getDocuments().get(0)));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding user by email: {}", email, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public User save(User user) {
        try {
            if (user.getId() == null) {
                user.setId(generateId());
            }
            firestore.collection(COLLECTION).document(user.getId())
                    .set(userToMap(user)).get();
            log.debug("Saved user: {}", user.getId());
            return user;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving user: {}", user.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error writing to Firestore", e);
        }
    }

    public User update(User user) {
        user.setUpdatedAt(Instant.now());
        return save(user);
    }

    private String generateId() {
        return "USR-" + String.format("%06d",
                (int) (Math.random() * 999999) + 1);
    }

    @SuppressWarnings("unchecked")
    private User documentToUser(DocumentSnapshot doc) {
        return User.builder()
                .id(doc.getString("id"))
                .email(doc.getString("email"))
                .displayName(doc.getString("displayName"))
                .phone(doc.getString("phone"))
                .status(doc.getString("status"))
                .roles(doc.get("roles") != null ? (java.util.List<String>) doc.get("roles") : java.util.List.of("USER"))
                .createdAt(toInstant(doc.getTimestamp("createdAt")))
                .updatedAt(toInstant(doc.getTimestamp("updatedAt")))
                .lastLoginAt(toInstant(doc.getTimestamp("lastLoginAt")))
                .build();
    }

    private Map<String, Object> userToMap(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "status", user.getStatus() != null ? user.getStatus() : "ACTIVE",
                "roles", user.getRoles() != null ? user.getRoles() : java.util.List.of("USER"),
                "createdAt", user.getCreatedAt() != null ? com.google.cloud.Timestamp.ofTimeSecondsAndNanos(user.getCreatedAt().getEpochSecond(), user.getCreatedAt().getNano()) : com.google.cloud.Timestamp.now(),
                "updatedAt", com.google.cloud.Timestamp.now(),
                "lastLoginAt", user.getLastLoginAt() != null ? com.google.cloud.Timestamp.ofTimeSecondsAndNanos(user.getLastLoginAt().getEpochSecond(), user.getLastLoginAt().getNano()) : com.google.cloud.Timestamp.now()
        );
    }

    private Instant toInstant(com.google.cloud.Timestamp timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
