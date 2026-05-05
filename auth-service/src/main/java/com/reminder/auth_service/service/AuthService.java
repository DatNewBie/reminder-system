package com.reminder.auth_service.service;

import com.reminder.auth_service.dto.AuthResponse;
import com.reminder.auth_service.dto.LoginRequest;
import com.reminder.auth_service.dto.RegisterRequest;
import com.reminder.auth_service.entity.RefreshToken;
import com.reminder.auth_service.entity.User;
import com.reminder.auth_service.exception.TokenReusedException;
import com.reminder.auth_service.exception.TokenRevokedException;
import com.reminder.auth_service.repository.RefreshTokenRepository;
import com.reminder.auth_service.repository.UserRepository;
import com.reminder.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsActive(true);

        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        
        storeRefreshToken(user.getId(), refreshToken, jwtUtil.generateTokenFamily());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        
        storeRefreshToken(user.getId(), refreshToken, jwtUtil.generateTokenFamily());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String tokenHash = jwtUtil.hashToken(refreshToken);
        
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (storedToken.isRevoked()) {
            revokeTokenFamily(storedToken.getTokenFamily());
            throw new TokenReusedException("Token reuse detected. All tokens in this session have been revoked.");
        }
        
        if (storedToken.isExpired()) {
            throw new RuntimeException("Refresh token expired");
        }
        
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }
        
        storedToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(storedToken);
        
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        
        storeRefreshToken(user.getId(), newRefreshToken, storedToken.getTokenFamily());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
    
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = jwtUtil.hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        storedToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(storedToken);
    }
    
    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now());
    }
    
    private void storeRefreshToken(UUID userId, String refreshToken, UUID tokenFamily) {
        String tokenHash = jwtUtil.hashToken(refreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(jwtUtil.extractExpiration(refreshToken).getTime() / 1000);
        
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(tokenHash);
        token.setTokenFamily(tokenFamily);
        token.setIssuedAt(OffsetDateTime.now());
        token.setExpiresAt(expiresAt);
        
        refreshTokenRepository.save(token);
    }
    
    private void revokeTokenFamily(UUID tokenFamily) {
        refreshTokenRepository.revokeByTokenFamily(tokenFamily, OffsetDateTime.now());
    }
}
