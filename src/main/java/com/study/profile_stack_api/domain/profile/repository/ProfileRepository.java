package com.study.profile_stack_api.domain.profile.repository;

import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByMember_Id(Long memberId);

    List<Profile> findAllByOrderByCreatedAtDescIdDesc();

    List<Profile> findByPositionOrderByCreatedAtDescIdDesc(Position position);

    Page<Profile> findByPosition(Position position, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
        SELECT p FROM Profile p
        WHERE (:nameKeyword IS NULL OR :nameKeyword = '' OR p.name LIKE concat('%', :nameKeyword, '%'))
        AND (:position IS NULL OR p.position = :position)
        ORDER BY p.createdAt DESC, p.id DESC
        """)
    Page<Profile> search(
            @Param("nameKeyword") String nameKeyword,
            @Param("position") Position position,
            Pageable pageable
    );
}
