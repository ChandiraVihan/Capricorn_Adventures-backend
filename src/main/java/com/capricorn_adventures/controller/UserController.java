package com.capricorn_adventures.controller;

import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.dto.ProfileUpdateRequestDTO;
import com.capricorn_adventures.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        UUID userId = UUID.fromString(auth.getName());
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody ProfileUpdateRequestDTO request) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        UUID userId = UUID.fromString(auth.getName());
        User user = userRepository.findById(userId).orElseThrow();

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        
        // Email update might require verification, but let's keep it simple for now or check if exists
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
                return ResponseEntity.badRequest().body("Email already in use");
            }
            user.setEmail(request.getEmail().toLowerCase().trim());
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
