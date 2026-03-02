package com.study.profile_stack_api.domain.techstack.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기술 스택 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "id", "profileId", "name",
        "category", "categoryIcon",
        "proficiency", "proficiencyIcon",
        "yearsOfExp", "createdAt", "updatedAt"
})
public class TechStackResponse {
    private Long id;
    private Long profileId;
    private String name;
    private String category;
    private String categoryIcon;
    private String proficiency;
    private String proficiencyIcon;
    private Integer yearsOfExp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
