package com.nexabank.api.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.nexabank.api.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Repository para la colección {@code accounts} de Firestore.
 */
@Repository
public class AccountRepository {

    private static final Logger log = LoggerFactory.getLogger(AccountRepository.class);
    private static final String COLLECTION = "accounts";

    private final Firestore firestore;

    public AccountRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<Account> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) {
                return Optional.of(documentToAccount(doc));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding account: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public List<Account> findByUserId(String userId) {
        try {
            QuerySnapshot query = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get().get();
            return query.getDocuments().stream()
                    .map(this::documentToAccount)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding accounts for user: {}", userId, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public Account save(Account account) {
        try {
            if (account.getId() == null) {
                account.setId(generateId());
            }
            firestore.collection(COLLECTION).document(account.getId())
                    .set(accountToMap(account)).get();
            log.debug("Saved account: {}", account.getId());
            return account;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving account: {}", account.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error writing to Firestore", e);
        }
    }

    public void updateBalance(String id, BigDecimal newBalance) {
        try {
            firestore.collection(COLLECTION).document(id).update(
                    Map.of(
                            "balance", newBalance.doubleValue(),
                            "updatedAt", Timestamp.now()
                    )
            ).get();
            log.debug("Updated balance for account {}: {}", id, newBalance);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating balance for account: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error writing to Firestore", e);
        }
    }

    public void updateStatus(String id, String status) {
        try {
            firestore.collection(COLLECTION).document(id).update(
                    Map.of(
                            "status", status,
                            "updatedAt", Timestamp.now()
                    )
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating status for account: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error writing to Firestore", e);
        }
    }

    private String generateId() {
        return "ACC-" + String.format("%06d",
                (int) (Math.random() * 999999) + 1);
    }

    private Account documentToAccount(DocumentSnapshot doc) {
        return Account.builder()
                .id(doc.getString("id"))
                .userId(doc.getString("userId"))
                .type(doc.getString("type"))
                .name(doc.getString("name"))
                .currency(doc.getString("currency"))
                .balance(doc.getDouble("balance") != null ? BigDecimal.valueOf(doc.getDouble("balance")) : BigDecimal.ZERO)
                .status(doc.getString("status"))
                .createdAt(toInstant(doc.getTimestamp("createdAt")))
                .updatedAt(toInstant(doc.getTimestamp("updatedAt")))
                .build();
    }

    private Map<String, Object> accountToMap(Account account) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", account.getId());
        map.put("userId", account.getUserId());
        map.put("type", account.getType());
        map.put("name", account.getName() != null ? account.getName() : "");
        map.put("currency", account.getCurrency());
        map.put("balance", account.getBalance().doubleValue());
        map.put("status", account.getStatus());
        map.put("createdAt", account.getCreatedAt() != null ? Timestamp.ofTimeSecondsAndNanos(account.getCreatedAt().getEpochSecond(), account.getCreatedAt().getNano()) : Timestamp.now());
        map.put("updatedAt", Timestamp.now());
        return map;
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
