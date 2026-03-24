package com.study.profile_stack_api.domain.auth.dao;

import com.study.profile_stack_api.domain.auth.entity.RefreshToken;

import java.util.Optional;

/**
 * Refresh Token Dao 인터페이스
 */
public interface RefreshTokenDao {

    // ==================== CREATE ====================

    void saveRefreshToken(RefreshToken refreshToken);

    // ==================== READ ====================

    Optional<RefreshToken> findByMemberId(Long memberId);

    // ==================== DELETE ====================

    void deleteRefreshToken(String refreshToken);

    void deleteByMemberId(Long memberId);

    int deleteExpiredRefreshTokens();
}
