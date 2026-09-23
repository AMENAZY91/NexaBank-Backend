package com.nexabank.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.client-email:}")
    private String clientEmail;

    @Value("${firebase.private-key:}")
    private String privateKey;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions options;

                InputStream credentialsStream = loadCredentialsStream();

                if (credentialsStream != null) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                            .build();
                    log.info("Firebase initialized from credentials file");
                } else if (privateKey != null && !privateKey.isBlank()) {
                    String serviceAccountJson = buildServiceAccountJson();
                    InputStream stream = new ByteArrayInputStream(
                            serviceAccountJson.getBytes(StandardCharsets.UTF_8));

                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(stream))
                            .setProjectId(projectId)
                            .build();
                    log.info("Firebase initialized from environment variables");
                } else {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(projectId)
                            .build();
                    log.info("Firebase initialized from Application Default Credentials");
                }

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully for project: {}",
                        FirebaseApp.getInstance().getOptions().getProjectId());
            } catch (IOException e) {
                log.error("Failed to initialize Firebase", e);
                throw new RuntimeException("Failed to initialize Firebase", e);
            }
        }
    }

    private InputStream loadCredentialsStream() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                log.info("Loading Firebase credentials from classpath");
                return resource.getInputStream();
            }
        } catch (IOException e) {
            log.debug("No firebase-service-account.json found in classpath");
        }
        return null;
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    private String buildServiceAccountJson() {
        String formattedKey = privateKey.replace("\\n", "\n");

        return """
                {
                  "type": "service_account",
                  "project_id": "%s",
                  "client_email": "%s",
                  "private_key": "%s",
                  "token_uri": "https://oauth2.googleapis.com/token"
                }
                """.formatted(projectId, clientEmail, formattedKey);
    }
}
