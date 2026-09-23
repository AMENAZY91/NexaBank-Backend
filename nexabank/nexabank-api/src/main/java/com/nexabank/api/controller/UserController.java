package com.nexabank.api.controller;

import com.nexabank.api.dto.user.UserResponse;
import com.nexabank.api.dto.user.UserUpdateRequest;
import com.nexabank.api.service.UserService;
import com.nexabank.api.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String userId = securityUtils.getCurrentUserId();
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@RequestBody UserUpdateRequest request) {
        String userId = securityUtils.getCurrentUserId();
        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }
}
