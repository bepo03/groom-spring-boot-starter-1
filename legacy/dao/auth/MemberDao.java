package com.study.profile_stack_api.domain.auth.dao;

import com.study.profile_stack_api.domain.auth.entity.Member;

import java.util.Optional;

/**
 * Member Dao 인터페이스
 */
public interface MemberDao {

    // ==================== CREATE ====================

    Member save(Member member);

    // ==================== READ ====================

    Optional<Member> findById(Long id);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByRefreshToken(String refreshToken);

    // ==================== UPDATE ====================

    int update(Member member);

    // ==================== DELETE ====================

    int deleteById(Long id);

    // ==================== VALIDATION ====================

    boolean existsByUsername(String username);
}
