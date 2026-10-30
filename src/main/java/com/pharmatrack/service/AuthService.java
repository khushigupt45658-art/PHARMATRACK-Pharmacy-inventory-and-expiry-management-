package com.pharmatrack.service;

import com.pharmatrack.config.JwtTokenProvider;
import com.pharmatrack.dto.AuthRequest;
import com.pharmatrack.dto.AuthResponse;
import com.pharmatrack.dto.RegisterRequest;
import com.pharmatrack.dto.UserProfileDto;
import com.pharmatrack.exception.BadRequestException;
import com.pharmatrack.exception.ResourceNotFoundException;
import com.pharmatrack.model.Role;
import com.pharmatrack.model.User;
import com.pharmatrack.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        // Search by username or email
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!user.isActive()) {
            throw new BadRequestException("Account has been deactivated. Please contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = tokenProvider.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // New registrations are always CUSTOMER role for security
        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null,
                Role.ROLE_CUSTOMER
        );

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser);

        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole().name());
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(User user) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserProfileDto(
                freshUser.getId(),
                freshUser.getUsername(),
                freshUser.getEmail(),
                freshUser.getFullName(),
                freshUser.getPhoneNumber(),
                freshUser.getRole().name(),
                freshUser.isActive()
        );
    }
}
