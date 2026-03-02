package com.study.profile_stack_api.domain.profile.repository;

import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.domain.profile.exception.ProfileNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 프로필 저장소
 */
@Repository
public class ProfileRepository {
    /** 데이터 저장소 */
    private final Map<Long, Profile> database = new HashMap<>();

    /** ID 자동 증가 시퀀스 */
    private final AtomicLong sequence = new AtomicLong(1);

    // ==================== CREATE ====================

    /**
     * 프로필 저장
     *
     * @param profile 저장할 프로필
     * @return 저장된 프로필 (ID 포함)
     */
    public Profile save(Profile profile) {
        // ID 없는 경우 생성
        if (profile.getId() == null) {
            profile.setId(sequence.getAndIncrement());
        }
        database.put(profile.getId(), profile);
        return profile;
    }

    // ==================== READ ====================

    /**
     * 전체 프로필 조회 (최신순 정렬)
     *
     * @return 모든 프로필 리스트
     */
    public List<Profile> findAll() {
        return database.values().stream()
                .sorted(Comparator.comparing(Profile::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * ID로 프로필 조회
     *
     * @param id 조회할 프로필 ID
     * @return 프로필 (없다면 Null)
     */
    public Optional<Profile> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    /**
     * 직무로 프로필 조회
     *
     * @param position 조회할 직무
     * @return 직무별 프로필 리스트
     */
    public List<Profile> findByPosition(Position position) {
        return database.values().stream()
                .filter(profile -> profile.getPosition().equals(position))
                .sorted(Comparator.comparing(Profile::getCreatedAt))
                .collect(Collectors.toList());
    }

    // ==================== UPDATE ====================

    /**
     * 프로필 수정
     */
    public Profile update(Profile profile) {
        if (profile.getId() == null) {
            throw new IllegalArgumentException("수정할 프로필의 ID가 없습니다.");
        }
        if (!database.containsKey(profile.getId())) {
            throw new ProfileNotFoundException(profile.getId());
        }
        database.put(profile.getId(), profile);
        return profile;
    }

    // ==================== DELETE ====================

    /**
     * ID로 프로필 삭제
     *
     * @param id 삭제할 프로필 ID
     * @return 삭제 성공 여부 (true: 삭제됨, false: 해당 ID 없음)
     */
    public boolean deleteById(Long id) {
        Profile removed = database.remove(id);
        return removed != null;
    }

    /**
     * 전체 프로필 삭제
     *
     * @return 삭제된 프로필 수
     */
    public int deleteAll() {
        int count = database.size();
        database.clear();
        return count;
    }

    // ==================== VALIDATION ====================

    /**
     * 이메일에 해당하는 프로필이 있는지 확인
     *
     * @param email 확인할 프로필 이메일
     * @return 존재여부
     */
    public boolean existsByEmail(String email) {
        return database.values().stream()
                .anyMatch(profile -> profile.getEmail() != null
                        && profile.getEmail().equalsIgnoreCase(email));
    }

    /**
     * ID에 해당하는 프로필이 있는지 확인
     *
     * @param id 확인할 프로필 ID
     * @return 존재여부
     */
    public boolean existsById(Long id) {
        return database.containsKey(id);
    }

    // ==================== LIFECYCLE CALLBACK ====================

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("🚀 ProfileRepository 초기화 완료!");
        System.out.println(" - 데이터 저장소(MAP) 준비됨");
        System.out.println(" - ID 생성기 준비됨");
        System.out.println("========================================");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("========================================");
        System.out.println("🔚 ProfileRepository 정리 중...");
        System.out.println(" - 저장된 데이터 수: " + database.size() + "개");
        System.out.println(" - 마지막 ID: " + (sequence.get() - 1));
        database.clear();
        System.out.println(" - 데이터 정리 완료!");
        System.out.println("========================================");
    }
}
