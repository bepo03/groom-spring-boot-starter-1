package com.study.profile_stack_api.domain.profile.dao;

import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.global.common.Page;

import java.util.List;
import java.util.Optional;

/**
 * Profile DAO 인터페이스
 */
public interface ProfileDao {

    // ==================== CREATE ====================

    Profile save(Profile profile);

    // ==================== READ ====================

    List<Profile> findAll();

    Optional<Profile> findById(Long id);

    Optional<Profile> findByMemberId(Long id);

    List<Profile> findByPosition(Position position);

    // ==================== PAGING ====================

    Page<Profile> findAllWithPaging(int page, int size);

    Page<Profile> findByPositionWithPaging(String position, int page, int size);

    Page<Profile> searchWithPaging(String nameKeyword, String position, int page, int size);

    // ==================== UPDATE ====================

    Profile update(Profile profile);

    // ==================== DELETE ====================

    boolean deleteById(Long id);

    int deleteAll();

    // ==================== VALIDATION ====================

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(Long id, String email);

    boolean existsById(Long id);
}
