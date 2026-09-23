package com.nexabank.api.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.nexabank.api.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Repository para la colección {@code transactions} de Firestore.
 *
 * <p>No incluye operación {@code delete} para transacciones COMPLETED.
 * Las transacciones completadas no se eliminan físicamente.
 */
@Repository
public class TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepository.class);
    private static final String COLLECTION = "transactions";

    private final Firestore firestore;

    public TransactionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<Transaction> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (doc.exists()) {
                return Optional.of(documentToTransaction(doc));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding transaction: {}", id, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public List<Transaction> findByAccountId(String accountId) {
        try {
            QuerySnapshot query = firestore.collection(COLLECTION)
                    .whereEqualTo("accountId", accountId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get().get();
            return query.getDocuments().stream()
                    .map(this::documentToTransaction)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding transactions for account: {}", accountId, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public List<Transaction> findByUserId(String userId) {
        try {
            QuerySnapshot query = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get().get();
            return query.getDocuments().stream()
                    .map(this::documentToTransaction)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding transactions for user: {}", userId, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error accessing Firestore", e);
        }
    }

    public Transaction save(Transaction transaction) {
        try {
            if (transaction.getId() == null) {
                transaction.setId(generateId());
            }
            firestore.collection(COLLECTION).document(transaction.getId())
                    .set(transactionToMap(transaction)).get();
            log.debug("Saved transaction: {}", transaction.getId());
            return transaction;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving transaction: {}", transaction.getId(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error writing to Firestore", e);
        }
    }

    private String generateId() {
        return "TX-" + String.format("%06d",
                (int) (Math.random() * 999999) + 1);
    }

    private Transaction documentToTransaction(DocumentSnapshot doc) {
        return Transaction.builder()
                .id(doc.getString("id"))
                .accountId(doc.getString("accountId"))
                .userId(doc.getString("userId"))
                .type(doc.getString("type"))
                .category(doc.getString("category"))
                .amount(doc.getDouble("amount") != null ? BigDecimal.valueOf(doc.getDouble("amount")) : BigDecimal.ZERO)
                .currency(doc.getString("currency"))
                .description(doc.getString("description"))
                .status(doc.getString("status"))
                .timestamp(toInstant(doc.getTimestamp("timestamp")))
                .createdAt(toInstant(doc.getTimestamp("createdAt")))
                .build();
    }

    private Map<String, Object> transactionToMap(Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("accountId", tx.getAccountId());
        map.put("userId", tx.getUserId());
        map.put("type", tx.getType());
        map.put("category", tx.getCategory() != null ? tx.getCategory() : "");
        map.put("amount", tx.getAmount().doubleValue());
        map.put("currency", tx.getCurrency());
        map.put("description", tx.getDescription() != null ? tx.getDescription() : "");
        map.put("status", tx.getStatus());
        map.put("timestamp", tx.getTimestamp() != null ? Timestamp.ofTimeSecondsAndNanos(tx.getTimestamp().getEpochSecond(), tx.getTimestamp().getNano()) : Timestamp.now());
        map.put("createdAt", tx.getCreatedAt() != null ? Timestamp.ofTimeSecondsAndNanos(tx.getCreatedAt().getEpochSecond(), tx.getCreatedAt().getNano()) : Timestamp.now());
        return map;
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
