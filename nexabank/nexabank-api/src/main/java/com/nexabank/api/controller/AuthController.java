package com.nexabank.api.controller;

import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final FirebaseAuth firebaseAuth;

    public AuthController(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) throws FirebaseAuthException {
        String token = authorizationHeader.replace("Bearer ", "");
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        return ResponseEntity.ok(Map.of(
                "uid", decodedToken.getUid(),
                "email", decodedToken.getEmail(),
                "displayName", decodedToken.getName(),
                "claims", decodedToken.getClaims()
        ));
    }
}
