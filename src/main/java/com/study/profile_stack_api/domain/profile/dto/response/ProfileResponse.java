package com.study.profile_stack_api.domain.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로필 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "id", "name", "email",
        "bio", "position", "positionIcon",
        "careerYears", "githubUrl", "blogUrl",
        "createdAt", "updatedAt"
})
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String position;
    private String positionIcon;
    private Integer careerYears;
    private String githubUrl;
    private String blogUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
