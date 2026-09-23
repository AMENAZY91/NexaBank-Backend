package com.nexabank.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de NexaBank API.
 *
 * <p>Esta aplicación Spring Boot es la capa de producción que:
 * <ul>
 *   <li>Expone una API REST sobre HTTP/JSON</li>
 *   <li>Persiste información financiera en Firebase Firestore</li>
 *   <li>Autentica y autoriza usuarios via Firebase Auth</li>
 *   <li>Integra nexabank-core para algoritmos académicos</li>
 * </ul>
 */
@SpringBootApplication
public class NexabankApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexabankApiApplication.class, args);
    }

}
