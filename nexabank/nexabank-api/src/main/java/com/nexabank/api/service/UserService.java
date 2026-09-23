package com.nexabank.api.service;

import com.nexabank.api.dto.user.UserResponse;
import com.nexabank.api.dto.user.UserUpdateRequest;
import com.nexabank.api.exception.ResourceNotFoundException;
import com.nexabank.api.mapper.UserMapper;
import com.nexabank.api.model.User;
import com.nexabank.api.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userMapper.updateFromRequest(request, user);
        User updated = userRepository.update(user);
        return userMapper.toResponse(updated);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
