package com.study.profile_stack_api.domain.profile.service;

import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.Role;
import com.study.profile_stack_api.domain.auth.repository.MemberRepository;
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
import com.study.profile_stack_api.domain.profile.repository.ProfileRepository;
import com.study.profile_stack_api.global.common.Page;
import com.study.profile_stack_api.global.discord.service.DiscordNotificationService;
import com.study.profile_stack_api.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final MemberRepository memberRepository;
    private final DiscordNotificationService discordNotificationService;

    /** 페이징 관련 상수 */
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== CREATE ====================

    /**
     * 프로필 생성
     * @param request 생성 요청 DTO
     * @return 생성된 프로필 응담 DTO
     */
    public ProfileResponse createProfile(ProfileCreateRequest request, String currentUsername) {
        // 이메일 유효성 검증
        if (profileRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // DTO -> Entity변환
        Profile profile = profileMapper.toEntity(request);
        Member member = getCurrentMember(currentUsername);
        profile.setMember(member);

        // 저장
        Profile savedProfile = profileRepository.save(profile);

        // Discord 알림 전송 (비동기)
        try {
            discordNotificationService.sendProfileCreatedNotification(savedProfile);
        } catch (Exception e) {
            // Discord 실패가 메인 로직에 영향을 주지 않음
            log.warn("Discord notification failed, but Profile was created", e);
        }

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
        List<Profile> profiles = profileRepository.findAllByOrderByCreatedAtDescIdDesc();

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
        List<Profile> profiles = profileRepository.findAllByOrderByCreatedAtDescIdDesc();

        // 이름 검색어가 없거나 빈값이 아니면 필터링
        if (nameKeyword != null && !nameKeyword.isBlank()) {
            profiles = filterProfiles( profiles, nameKeyword, (profile) ->
                    profile.getName().contains(nameKeyword)
            );
        }

        // 직무 검색어가 없거나 빈값이 아니면 필터링
        if (positionKeyword != null && !positionKeyword.isBlank()) {
            Position position = parsePosition(positionKeyword);
            profiles = filterProfiles(profiles, positionKeyword, (profile) ->
                    profile.getPosition() == position
            );
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
        Profile profile = profileRepository.findById(id)
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
        Position position = parsePosition(positionName);

        // Repository에서 직무로 조회
        List<Profile> profiles = profileRepository.findByPositionOrderByCreatedAtDescIdDesc(position);

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
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        org.springframework.data.domain.Page<Profile> profilePage =
                profileRepository.findAll(pageable);

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
        Position position = parsePosition(positionName);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        org.springframework.data.domain.Page<Profile> profilePage =
                profileRepository.findByPosition(position, pageable);

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

        Position position = null;
        if (positionKeyword != null && !positionKeyword.isBlank()) {
            position = parsePosition(positionKeyword);
        }

        // DAO에서 페이징된 Entity 조회
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        org.springframework.data.domain.Page<Profile> profilePage =
                profileRepository.search(nameKeyword, position, pageable);

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

    /**
     * 프로필 수정
     *
     * @param id 수정할 프로필 ID
     * @param request 수정 요청 데이터
     * @return 수정된 프로필 응답
     */
    public ProfileResponse updateProfile(Long id, ProfileUpdateRequest request, String currentUsername) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(request);

        // 기존 프로필 조회
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        // 소유권 검증: 현재 로그인한 사용자가 이 프로필의 소유주인지 확인
        if (!profile.getMember().equals(getCurrentMember(currentUsername))) {
            throw new AuthException("본인의 프로필만 수정할 수 있습니다.");
        }

        // 직무 유효성 검증
        if (request.getPosition() != null) {
            request.setPosition(parsePositionName(request.getPosition()));
        }

        // Entity 업데이트 (Null이 아닌 값만 반영)
        profileMapper.updateEntity(request, profile);

        // 저장 및 응답 반환
        Profile updatedProfile = profileRepository.save(profile);

        // Discord 알림 전송 (비동기)
        try {
            discordNotificationService.sendProfileUpdatedNotification(updatedProfile);
        } catch (Exception e) {
            // Discord 실패가 메인 로직에 영향을 주지 않음
            log.warn("Discord notification failed, but Profile was updated", e);
        }

        return profileMapper.toResponse(updatedProfile);
    }

    // ==================== DELETE ====================

    /**
     * 프로필 한개를 삭제
     *
     * @param id 삭제할 프로필 ID
     * @return 삭제 결과  응답
     */
    public ProfileDeleteResponse deleteProfile(Long id, String currentUsername) {
        // ID에 따른 프로필이 있는지 확인
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        // 소유권 검증: 현재 로그인한 사용자가 이 프로필의 소유주인지 확인
        if (!profile.getMember().equals(getCurrentMember(currentUsername))) {
            throw new AuthException("본인의 프로필만 삭제할 수 있습니다.");
        }

        // 삭제 수행
        profileRepository.delete(profile);

        // Discord 알림 전송 (비동기)
        try {
            Long profileId = profile.getId();
            String profileName = profile.getName();
            discordNotificationService.sendProfileDeletedNotification(profileId, profileName);
        } catch (Exception e) {
            // Discord 실패가 메인 로직에 영향을 주지 않음
            log.warn("Discord notification failed, but Profile was deleted", e);
        }

        // 삭제 결과 반환
        return ProfileDeleteResponse.of(id, true);
    }

    /**
     * 전체 프로필 삭제
     *
     * @return 삭제 결과 응답
     */
    public ProfileDeleteAllResponse deleteAllProfiles(String currentUsername) {
        // 사용자 권한 확인
        Role role = memberRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."))
                .getRole();

        // 관리자만 전체 삭제
        if (role != Role.ADMIN) {
            throw new AuthException("관리자만 전체 삭제를 할 수 있습니다.");
        }

        // 프로필 총 개수 확인
        long deleteCount = profileRepository.count();
        profileRepository.deleteAll();

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
    private List<Profile> filterProfiles(
            List<Profile> profiles,
            String keyword,
            Predicate<Profile> predicate
    ) {
        if (keyword == null || keyword.isBlank()) {
            return profiles;
        }

        return profiles.stream()
                .filter(predicate)
                .toList();
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param username 현재 로그인한 사용자 이름
     * @return 회원 엔티티
     */
    private Member getCurrentMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 직무 문자열을 Position으로 변환
     */
    private Position parsePosition(String positionName) {
        try {
            return Position.valueOf(positionName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 직무입니다: " + positionName);
        }
    }

    /**
     * 직무 문자열을 DB 저장용 enum 이름으로 변환
     */
    private String parsePositionName(String positionName) {
        return parsePosition(positionName).name();
    }
}
