package com.reminder.auth_service.repository;

import com.reminder.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    List<RefreshToken> findAllByUserId(UUID userId);
    
    List<RefreshToken> findAllByTokenFamily(UUID tokenFamily);
    
    void deleteByUserId(UUID userId);
    
    void deleteByExpiresAtBefore(OffsetDateTime dateTime);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.tokenFamily = :tokenFamily AND rt.revokedAt IS NULL")
    void revokeByTokenFamily(@Param("tokenFamily") UUID tokenFamily, @Param("revokedAt") OffsetDateTime revokedAt);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") OffsetDateTime revokedAt);
}
