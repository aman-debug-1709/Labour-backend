package com.labournow.backend.service;

import com.labournow.backend.dto.AuthRequest;
import com.labournow.backend.dto.AuthResponse;
import com.labournow.backend.entity.User;
import com.labournow.backend.repository.UserRepository;
import com.labournow.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse loginOrRegister(AuthRequest request) {
        // Mock OTP validation - assume OTP is always correct in MVP if not empty
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            throw new RuntimeException("OTP is required");
        }

        User user = userRepository.findByPhone(request.getPhone()).orElse(null);

        if (user == null) {
            // Register new user
            if (request.getName() == null || request.getRole() == null) {
                throw new RuntimeException("Name and Role required for new registration");
            }
            
            user = User.builder()
                    .phone(request.getPhone())
                    .name(request.getName())
                    .role(User.Role.valueOf(request.getRole().toUpperCase()))
                    .isActive(true)
                    .build();
                    
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getPhone());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole().name())
                .message("Authentication successful")
                .build();
    }
}
