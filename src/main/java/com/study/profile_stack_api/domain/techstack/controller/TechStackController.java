package com.study.profile_stack_api.domain.techstack.controller;

import com.study.profile_stack_api.domain.techstack.dto.request.TechStackCreateRequest;
import com.study.profile_stack_api.domain.techstack.dto.request.TechStackUpdateRequest;
import com.study.profile_stack_api.domain.techstack.dto.response.TechStackDeleteResponse;
import com.study.profile_stack_api.domain.techstack.dto.response.TechStackResponse;
import com.study.profile_stack_api.domain.techstack.service.TechStackService;
import com.study.profile_stack_api.global.common.ApiResponse;
import com.study.profile_stack_api.global.common.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 기술 스택 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles/{profileId}/tech-stacks")
@RequiredArgsConstructor
public class TechStackController {
    /** 의존성 주입: Service */
    private final TechStackService techStackService;

    // ==================== CREATE ====================

    /**
     * 프로필별 기술 스택 생성
     * POST /api/v1/profiles/{profileId}/tech-stacks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TechStackResponse>> createTechStackByProfileId(
            @PathVariable
            Long profileId,
            @Valid
            @RequestBody
            TechStackCreateRequest request
    ) {
        // Service 호출하여 기술 스택 생성
        TechStackResponse response = techStackService.createTechStackByProfileId(profileId, request);

        // 201 CREATED 상태코드와 함께 응답
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ==================== READ ====================

    /**
     * 프로필별 기술 스택 전체 조회
     * GET /api/v1/profiles/{profileId}/tech-stacks
     * GET /api/v1/profiles/{profileId}/tech-stacks?category={category}
     * GET /api/v1/profiles/{profileId}/tech-stacks?proficiency={proficiency}
     */
    @GetMapping(params = {"!page", "!size"})
    public ResponseEntity<ApiResponse<List<TechStackResponse>>> getAllTechStacksByProfileId(
            @RequestParam(required = false)
            String category,
            @RequestParam(required = false)
            String proficiency,
            @PathVariable
            Long profileId
    ) {
        List<TechStackResponse> responses;

        if ((category != null && !category.isBlank()) || (proficiency != null && !proficiency.isBlank())) {
            // Service 호출하여 조건에 맞는 기술 스택 조회
            responses = techStackService.searchTechStackByProfileIdAndCategoryAndProficiency(profileId, category, proficiency);
        } else {
            // Service 호출하여 모든 기술 스택 조회
            responses = techStackService.getAllTechStacksByProfileId(profileId);
        }

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(responses));
    }

    /**
     * 프로필별 기술 스택 ID로 단건 조회
     * GET /api/v1/profiles/{profileId}/tech-stacks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechStackResponse>> getTechStackByProfileIdAndId(
            @PathVariable
            Long profileId,
            @PathVariable
            Long id
    ) {
        // Service 호출하여 ID로 기술 스택 조회
        TechStackResponse response = techStackService.getTechStackByProfileIdAndId(profileId, id);

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

    // ==================== PAGING ====================

    /**
     * 프로필별 전체 기술 스택 페이징 조회
     *
     * @param page 페이지 번호 (0-based, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10, 최대값: 100)
     * @return 페이징된 기술 스택
     */
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ApiResponse<Page<TechStackResponse>>> getTechStacksWithPaging(
            @PathVariable
            Long profileId,
            @RequestParam(required = false)
            String category,
            @RequestParam(required = false)
            String proficiency,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size
    ) {
        Page<TechStackResponse> response;

        if ((category != null && !category.isBlank()) || (proficiency != null && !proficiency.isBlank())) {
            // Service 호출하여 조건에 맞는 프로필별 기술 스택 페이징 조회
            response = techStackService.searchTechStackWithPaging(profileId, category, proficiency, page, size);
        } else {
            // Service 호출하여 프로필별 전체 기술 스택 페이징 조회
            response = techStackService.getTechStacksWithPaging(profileId, page, size);
        }

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

    /**
     * 프로필별 기술 카테고리로 기술 스택 페이징 조회
     *
     * @param category 기술 카테고리
     * @param page 페이지 번호 (0-based, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10, 최대값: 100)
     * @return 페이징된 기술 스택
     */
    @GetMapping(path = "/category/{category}", params = {"page", "size"})
    public ResponseEntity<ApiResponse<Page<TechStackResponse>>> getTechStacksByCategoryWithPaging(
            @PathVariable
            Long profileId,
            @PathVariable
            String category,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size
    ) {
        // Service 호출하여 직무별 프로필 페이징 조회
        Page<TechStackResponse> response = techStackService.getTechStacksByCategoryWithPaging(
                profileId, category, page, size);

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

    // ==================== UPDATE ====================

    /**
     * 프로필별 기술 스택 수정
     * PUT /api/v1/profiles/{profileId}/tech-stacks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechStackResponse>> updateTechStackByProfileId(
            @PathVariable
            Long profileId,
            @PathVariable
            Long id,
            @Valid
            @RequestBody
            TechStackUpdateRequest request
    ) {
        // Service 호출하여 ID로 기술 스택 수정
        TechStackResponse response = techStackService.updateTechStackByProfileId(profileId, id, request);

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== DELETE ====================

    /**
     * 프로필별 기술 스택 단건 삭제
     * DELETE /api/v1/profiles/{profileId}/tech-stacks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TechStackDeleteResponse>> deleteTechStackByProfileIdAndId(
            @PathVariable
            Long profileId,
            @PathVariable
            Long id
    ) {
        TechStackDeleteResponse response = techStackService.deleteTechStackByProfileIdAndId(profileId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필별 기술 스택 전체 삭제
     * DELETE /api/v1/profiles/{profileId}/tech-stacks
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllTechStackByProfileId(
            @PathVariable
            Long profileId
    ) {
        Map<String, Object> response = techStackService.deleteAllTechStackByProfileId(profileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
