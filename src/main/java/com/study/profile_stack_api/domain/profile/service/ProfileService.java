package com.study.profile_stack_api.domain.profile.service;

import com.study.profile_stack_api.domain.profile.dao.ProfileDao;
import com.study.profile_stack_api.domain.profile.dto.request.ProfileCreateRequest;
import com.study.profile_stack_api.domain.profile.dto.request.ProfileSearchCondition;
import com.study.profile_stack_api.domain.profile.dto.request.ProfileUpdateRequest;
import com.study.profile_stack_api.domain.profile.dto.response.ProfileDeleteAllResponse;
import com.study.profile_stack_api.domain.profile.dto.response.ProfileDeleteResponse;
import com.study.profile_stack_api.domain.profile.dto.response.ProfileResponse;
import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.domain.profile.exception.DuplicateEmailException;
import com.study.profile_stack_api.domain.profile.exception.ProfileNotFoundException;
import com.study.profile_stack_api.domain.profile.mapper.ProfileMapper;
import com.study.profile_stack_api.global.common.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 프로필 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    /** 의존성 주입 */
    private final ProfileDao profileDao;
    private final ProfileMapper profileMapper;

    /** 페이징 관련 상수 */
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== CREATE ====================

    /**
     * 프로필 생성
     * @param request 생성 요청 DTO
     * @return 생성된 프로필 응담 DTO
     */
    public ProfileResponse createProfile(ProfileCreateRequest request) {
        // 이메일 유효성 검증
        if (profileDao.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // DTO -> Entity변환
        Profile profile = profileMapper.toEntity(request);

        // 저장
        Profile savedProfile = profileDao.save(profile);

        // Entity -> Response DTO 변환 후 반환
        return profileMapper.toResponse(savedProfile);
    }

    // ==================== READ ====================

    /**
     * 전체 프로필 조회 (최신순 정렬)
     *
     * @return 모든 프로필 응답 DTO 리스트
     */
    public List<ProfileResponse> getAllProfiles() {
        // Repository에서 모든 프로필 조회
        List<Profile> profiles = profileDao.findAll();

        // Entity 리스트 -> Response DTO 리스트로 변환
        return profileMapper.toResponseList(profiles);
    }

    /**
     * 이름/직무 조건으로 프로필 조회
     *
     * @param nameKeyword 이름 검색어
     * @param positionKeyword 직무 검색어
     * @return 조건에 맞는 프로필 응답 DTO 리스트
     */
    public List<ProfileResponse> searchProfiles(String nameKeyword, String positionKeyword) {
        // Repository에서 모든 프로필 조회
        List<Profile> profiles = profileDao.findAll();

        // 이름 검색어가 없거나 빈값이 아니면 필터링
        if (nameKeyword != null && !nameKeyword.isBlank()) {
            profiles = filterProfiles(profiles, (profile) -> profile.getName().contains(nameKeyword));
        }

        // 직무 검색어가 없거나 빈값이 아니면 필터링
        if (positionKeyword != null && !positionKeyword.isBlank()) {
            Position position;
            try {
                position = Position.valueOf(positionKeyword.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 직무입니다: " + positionKeyword);
            }
            profiles = filterProfiles(profiles, (profile) -> profile.getPosition() == position);
        }

        // Entity 리스트 -> Response DTO 리스트로 변환
        return profileMapper.toResponseList(profiles);
    }

    /**
     * ID로 프로필 단건 조회
     *
     * @param id 조회할 프로필 ID
     * @return 프로필 응답 DTO
     */
    public ProfileResponse getProfileById(Long id) {
        // Repository에서 ID로 조회, 존재하지 않으면 예외 처리
        Profile profile = profileDao.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        // Entity -> Response DTO로 변환
        return profileMapper.toResponse(profile);
    }

    /**
     * 직무별 프로필 조회
     *
     * @param positionName 조회할 직무 이름
     * @return 직무별 프로필 응답 DTO 리스트
     */
    public List<ProfileResponse> getProfileByPosition(String positionName) {
        // 직무 이름으로 해당 직무 생성, 없는 직무면 예외 처리
        Position position;
        try {
            position = Position.valueOf(positionName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 직무입니다: " + positionName
            );
        }

        // Repository에서 직무로 조회
        List<Profile> profiles = profileDao.findByPosition(position);

        // Entity 리스트 -> Response DTO 리스트로 변환
        return profileMapper.toResponseList(profiles);
    }

    /**
     * 검색 상태 확인 후 조건에 따라 프로필 조회
     *
     * @param condition 프로필 검색 상태
     * @return 프로필 조회
     */
    public List<ProfileResponse> getSearchProfiles(ProfileSearchCondition condition) {
        if ((condition.getName() != null && !condition.getName().isBlank()) ||
                (condition.getPosition() != null && !condition.getPosition().isBlank())) {
            // 조건에 맞는 프로필 조회
            return searchProfiles(condition.getName(), condition.getPosition());
        } else {
            // 모든 프로필 조회
            return getAllProfiles();
        }
    }

    // ==================== PAGING ====================

    /**
     * 전체 프로필 페이징 조회
     *
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 프로필 응답
     */
    public Page<ProfileResponse> getProfilesWithPaging(int page, int size) {
        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        // DAO에서 페이징된 Entity 조회
        Page<Profile> profilePage = profileDao.findAllWithPaging(page, size);

        // Entity -> DTO 변환
        List<ProfileResponse> content = profileMapper.toResponseList(profilePage.getContent());

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, profilePage.getTotalElements());
    }

    /**
     * 직무별 프로필 페이징 조회
     *
     * @param positionName 직무 이름
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 프로필 응답
     */
    public Page<ProfileResponse> getProfilesByPositionWithPaging(String positionName, int page, int size) {
        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        // 직무 유효성 검증
        if (positionName == null || positionName.isBlank()) {
            return new Page<>(List.of(), page, size, 0);
        }

        // DAO에서 페이징된 Entity 조회
        Page<Profile> profilePage = profileDao.findByPositionWithPaging(positionName.toUpperCase(), page, size);

        // Entity -> DTO 변환
        List<ProfileResponse> content = profileMapper.toResponseList(profilePage.getContent());

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, profilePage.getTotalElements());
    }

    /**
     * 검색 조건에 맞는 데이터를 페이징 조회
     *
     * @param nameKeyword 이름 검색어
     * @param positionKeyword 직무 검색어
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 프로필 응답
     */
    public Page<ProfileResponse> searchProfilesWithPaging(String nameKeyword, String positionKeyword, int page, int size) {
        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        String position = null;
        if (positionKeyword != null && !positionKeyword.isBlank()) {
            try {
                position = Position.valueOf(positionKeyword.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 직무입니다: " + positionKeyword);
            }
        }

        // DAO에서 페이징된 Entity 조회
        Page<Profile> profilePage = profileDao.searchWithPaging(nameKeyword, position, page, size);

        // Entity -> DTO 변환
        List<ProfileResponse> content = profileMapper.toResponseList(profilePage.getContent());

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, profilePage.getTotalElements());

    }

    /**
     * 검색 상태 확인 후 조건에 따라 프로필 페이지 조회
     *
     * @param condition 프로필 검색 상태
     * @return 페이지 조회
     */
    public Page<ProfileResponse> getSearchProfilesWithPaging(ProfileSearchCondition condition) {
        if ((condition.getName() != null && !condition.getName().isBlank()) ||
                (condition.getPosition() != null && !condition.getPosition().isBlank())) {
            // 조건에 맞는 프로필 페이징 조회
            return searchProfilesWithPaging(
                    condition.getName(), condition.getPosition(), condition.getPage(), condition.getSize());
        } else {
            // 전체 프로필 페이징 조회
            return getProfilesWithPaging(condition.getPage(), condition.getSize());
        }
    }

    // ==================== UPDATE ====================

    public ProfileResponse updateProfile(Long id, ProfileUpdateRequest request) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(request);

        // 기존 프로필 조회
        Profile profile = profileDao.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        // 이메일 유효성 검증
        if (profileDao.existsByEmailAndIdNot(id, request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 직무 유효성 검증
        if (request.getPosition() != null) {
            try {
                request.setPosition(Position.valueOf(request.getPosition().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 직무 입니다.");
            }
        }

        // Entity 업데이트 (Null이 아닌 값만 반영)
        profileMapper.updateEntity(request, profile);

        // 저장 및 응답 반환
        Profile updatedProfile = profileDao.update(profile);
        return profileMapper.toResponse(updatedProfile);
    }

    // ==================== DELETE ====================

    /**
     * 프로필 한개를 삭제
     *
     * @param id 삭제할 프로필 ID
     * @return 삭제 결과  응답
     */
    public ProfileDeleteResponse deleteProfile(Long id) {
        // ID에 따른 프로필이 있는지 확인
        if (!profileDao.existsById(id)) {
            throw new ProfileNotFoundException(id);
        }

        // 삭제 수행
        boolean isDeleted = profileDao.deleteById(id);

        // 삭제 결과 반환
        return ProfileDeleteResponse.of(id, isDeleted);
    }

    /**
     * 전체 프로필 삭제
     *
     * @return 삭제 결과 응답
     */
    public ProfileDeleteAllResponse deleteAllProfiles() {
        // 프로필 총 개수 확인
        long deleteCount = profileDao.deleteAll();

        // 삭제 결과 반환
        return ProfileDeleteAllResponse.of(deleteCount);
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * 범용 필터
     *
     * @param profiles  프로필 리스트
     * @return 필터링된 프로필 리스트
     */
    private List<Profile> filterProfiles(List<Profile> profiles, Predicate<Profile> predicate) {
        profiles = profiles.stream()
                .filter(predicate)
                .toList();
        return profiles;
    }
}
