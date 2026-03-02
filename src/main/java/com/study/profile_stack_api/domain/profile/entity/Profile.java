package com.study.profile_stack_api.domain.profile.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 프로필 Entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private Position position;
    private Integer careerYears;
    private String githubUrl;
    private String blogUrl;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
