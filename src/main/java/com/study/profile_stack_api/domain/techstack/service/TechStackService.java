package com.study.profile_stack_api.domain.techstack.service;

import com.study.profile_stack_api.domain.profile.dao.ProfileDao;
import com.study.profile_stack_api.domain.profile.exception.ProfileNotFoundException;
import com.study.profile_stack_api.domain.techstack.dao.TechStackDao;
import com.study.profile_stack_api.domain.techstack.dto.request.TechStackCreateRequest;
import com.study.profile_stack_api.domain.techstack.dto.request.TechStackUpdateRequest;
import com.study.profile_stack_api.domain.techstack.dto.response.TechStackDeleteResponse;
import com.study.profile_stack_api.domain.techstack.dto.response.TechStackResponse;
import com.study.profile_stack_api.domain.techstack.entity.Proficiency;
import com.study.profile_stack_api.domain.techstack.entity.TechCategory;
import com.study.profile_stack_api.domain.techstack.entity.TechStack;
import com.study.profile_stack_api.domain.techstack.exception.TechStackNotFoundException;
import com.study.profile_stack_api.global.common.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 기술 스택 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechStackService {
    /** 의존성 주입: Repository */
    private final TechStackDao techStackDao;
    private final ProfileDao profileDao;

    /** 페이징 관련 상수 */
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== CREATE ====================

    /**
     * 프로필별 기술 스택 생성
     *
     * @param profileId 생성할 프로필 ID
     * @param request 생성 요청 DTO
     * @return 생성된 기술 스택 응담 DTO
     */
    public TechStackResponse createTechStackByProfileId(Long profileId, TechStackCreateRequest request) {

        // FK 검증
        validataProfileId(profileId);

        // DTO -> Entity변환
        TechStack techStack = TechStack.builder()
                .id(null)
                .profileId(profileId)
                .name(request.getName())
                .category(TechCategory.valueOf(request.getCategory()))
                .proficiency(Proficiency.valueOf(request.getProficiency()))
                .yearsOfExp(request.getYearsOfExp())
                .build();

        // 저장
        TechStack savedTechStack = techStackDao.saveByProfileId(profileId, techStack);

        // Entity -> Response DTO 변환 후 반환
        return TechStackResponse.from(savedTechStack);
    }

    // ==================== READ ====================

    /**
     * 프로필별 기술 스택 전체 조회 (최신순 정렬)
     *
     * @param profileId 조회할 프로필 ID
     * @return 프로필별 기술 스택 전체 응답 DTO 리스트
     */
    public List<TechStackResponse> getAllTechStacksByProfileId(Long profileId) {
        // FK 검증
        validataProfileId(profileId);

        // Repository에서 profileId에 해당하는 기술 스택만 조회
        List<TechStack> techStacks = techStackDao.findAllByProfileId(profileId);

        // Entity 리스트 -> Response DTO 리스트로 변환
        return techStacks.stream()
                .map(TechStackResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 프로필별 기술 카테고리/숙련도 조건으로 기술 스택 조회
     *
     * @param categoryKeyword 기술 카테고리 검색어
     * @param proficiencyKeyword 숙련도 검색어
     * @return 조건에 맞는 기술 스택 응답 DTO 리스트
     */
    public List<TechStackResponse> searchTechStackByProfileIdAndCategoryAndProficiency(
            Long profileId, String categoryKeyword, String proficiencyKeyword
    ) {
        // FK 검증
        validataProfileId(profileId);

        // Repository에서 모든 프로필 조회
        List<TechStack> techStacks = techStackDao.findAllByProfileId(profileId);

        // 기술 카테고리 검색어가 없거나 빈값이 아니면 필터링
        if (categoryKeyword != null && !categoryKeyword.isBlank()) {
            TechCategory category;
            try {
                category = TechCategory.valueOf(categoryKeyword.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 기술 카테고리입니다: " + categoryKeyword);
            }

            techStacks = techStacks.stream()
                    .filter(techStack -> techStack.getCategory() == category)
                    .toList();
        }

        // 숙련도 검색어가 없거나 빈값이 아니면 필터링
        if (proficiencyKeyword != null && !proficiencyKeyword.isBlank()) {
            Proficiency proficiency;
            try {
                proficiency = Proficiency.valueOf(proficiencyKeyword.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 숙련도입니다: " + proficiencyKeyword);
            }

            techStacks = techStacks.stream()
                    .filter(techStack -> techStack.getProficiency() == proficiency)
                    .toList();
        }

        // Entity 리스트 -> Response DTO 리스트로 변환
        return techStacks.stream()
                .map(TechStackResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 프로필별 기술 스택 ID로 단건 조회
     *
     * @param profileId 조회할 프로필 ID
     * @param id 조회할 기술 스택 ID
     * @return 기술 스택 응답 DTO
     */
    public TechStackResponse getTechStackByProfileIdAndId(Long profileId, Long id) {
        // FK 검증
        validataProfileId(profileId);

        // Repository에서 profileId + id로 조회, 존재하지 않으면 예외 처리
        TechStack techStack = techStackDao.findByProfileIdAndId(profileId, id)
                .orElseThrow(() -> new TechStackNotFoundException(id));

        // Entity -> Response DTO로 변환
        return TechStackResponse.from(techStack);
    }

    // ==================== PAGING ====================

    /**
     * 프로필별 전체 기술 스택 페이징 조회
     *
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 기술 스택 응답
     */
    public Page<TechStackResponse> getTechStacksWithPaging(Long profileId, int page, int size) {
        // FK 검증
        validataProfileId(profileId);

        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        // DAO에서 페이징된 Entity 조회
        Page<TechStack> techStackPage = techStackDao.findAllWithPaging(profileId, page, size);

        // Entity -> DTO 변환
        List<TechStackResponse> content = techStackPage.getContent().stream()
                .map(TechStackResponse::from)
                .toList();

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, techStackPage.getTotalElements());
    }

    /**
     * 프로필별 기술 카테고리로 기술 스택 페이징 조회
     *
     * @param categoryName 기술 카테고리 이름
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 기술 스택 응답
     */
    public Page<TechStackResponse> getTechStacksByCategoryWithPaging(
            Long profileId, String categoryName, int page, int size
    ) {
        // FK 검증
        validataProfileId(profileId);

        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        // 카테고리 유효성 검증
        if (categoryName == null || categoryName.isBlank()) {
            return new Page<>(List.of(), page, size, 0);
        }

        // DAO에서 페이징된 Entity 조회
        Page<TechStack> techStackPage = techStackDao.findByCategoryWithPaging(
                profileId, categoryName.toUpperCase(), page, size);

        // Entity -> DTO 변환
        List<TechStackResponse> content = techStackPage.getContent().stream()
                .map(TechStackResponse::from)
                .toList();

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, techStackPage.getTotalElements());
    }

    /**
     * 검색 조건에 맞는 데이터를 페이징 조회
     *
     * @param categoryKeyword 기술 카테고리 검색어
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 페이징된 기술 스택 응답
     */
    public Page<TechStackResponse> searchTechStackWithPaging(
            Long profileId, String categoryKeyword, String proficiencyKeyword, int page, int size) {
        // FK 검증
        validataProfileId(profileId);

        // 파라미터 유효성 검증
        page = Math.max(0, page);                           // 음수 방지
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);  // 1 ~ 100 범위

        String category = null;
        if (categoryKeyword != null && !categoryKeyword.isBlank()) {
            try {
                category = TechCategory.valueOf(categoryKeyword.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 기술 카테고리입니다: " + categoryKeyword);
            }
        }

        String proficiency = null;
        if (proficiencyKeyword != null && !proficiencyKeyword.isBlank()) {
            try {
                proficiency = Proficiency.valueOf(proficiencyKeyword.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 숙련도입니다: " + proficiencyKeyword);
            }
        }

        // DAO에서 페이징된 Entity 조회
        Page<TechStack> techStackPage = techStackDao.searchWithPaging(profileId, category, proficiency, page, size);

        // Entity -> DTO 변환
        List<TechStackResponse> content = techStackPage.getContent().stream()
                .map(TechStackResponse::from)
                .toList();

        // Page<Entity>를 Page<DTO>로 변환 및 반환
        return new Page<>(content, page, size, techStackPage.getTotalElements());

    }

    // ==================== UPDATE ====================

    /**
     * 프로필별 기술 스택 수정
     *
     * @param profileId 수정할 프로필 ID
     * @param id 수정할 기술 스택 ID
     * @param request 수정 요청 데이터
     * @return 수정된 기술 스택 응답
     */
    public TechStackResponse updateTechStackByProfileId(Long profileId, Long id, TechStackUpdateRequest request) {
        Objects.requireNonNull(profileId);
        Objects.requireNonNull(id);
        Objects.requireNonNull(request);

        // FK 검증
        validataProfileId(profileId);

        // 기존 기술 스택 조회
        TechStack techStack = techStackDao.findByProfileIdAndId(profileId, id)
                .orElseThrow(() -> new TechStackNotFoundException(id));

        // 수정 내용 있는지 확인
        if (request.hashNoUpdates()) {
            throw new IllegalArgumentException("수정 내용이 없습니다.");
        }

        // 기술 카테고리 및 숙련도 변환 (Null 아닌 경우에만)
        TechCategory category = null;
        Proficiency proficiency = null;
        if (request.getCategory() != null) {
            try {
                category = TechCategory.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 기술 스택 입니다.");
            }
        }

        if (request.getProficiency() != null) {
            try {
                proficiency = Proficiency.valueOf(request.getProficiency().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 숙련도 입니다.");
            }
        }

        // Entity 업데이트 (Null이 아닌 값만 반영)
        techStack.update(
                request.getName(),
                category,
                proficiency,
                request.getYearsOfExp()
        );

        // 저장 및 응답 반환
        TechStack updatedTechStack = techStackDao.updateByProfileId(profileId, techStack);
        return TechStackResponse.from(updatedTechStack);
    }

    // ==================== DELETE ====================

    /**
     * 프로필별 기술 스택 단건 삭제
     *
     * @param profileId 삭제할 프로필 ID
     * @param id 삭제할 기술 스택 ID
     * @return 삭제 결과  응답
     */
    public TechStackDeleteResponse deleteTechStackByProfileIdAndId(Long profileId, Long id) {
        // FK 검증
        validataProfileId(profileId);

        // ProfileId + ID에 따른 기술 스택이 있는지 확인
        techStackDao.findByProfileIdAndId(profileId, id)
                .orElseThrow(() -> new TechStackNotFoundException(id));

        // 삭제 수행
        boolean isDeleted = techStackDao.deleteByProfileIdAndId(profileId, id);

        // 삭제 결과 반환
        return TechStackDeleteResponse.of(id, isDeleted);
    }

    /**
     * 프로필별 기술 스택 전체 삭제
     *
     * @param profileId 삭제할 프로필 ID
     * @return 삭제 결과 응답
     */
    public Map<String, Object> deleteAllTechStackByProfileId(Long profileId) {
        // FK 검증
        validataProfileId(profileId);

        // 프로필 총 개수 확인
        long deleteCount = techStackDao.deleteAllByProfileId(profileId);

        // 삭제 결과 반환
        return Map.of(
                "message", "전체 기술 스택이 성공적으로 삭제되었습니다.",
                "deletedCount", deleteCount
        );
    }

    // ==================== VALIDATION ====================

    /**
     * 프로필과 기술 스택의 FK 검증
     */
    private void validataProfileId(Long profileId) {
        if (!profileDao.existsById(profileId)) {
            throw new ProfileNotFoundException(profileId);
        }
    }
}
