package com.study.profile_stack_api.domain.auth.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 회원 Entity
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class Member {
    private Long id;
    private String username;
    private String password;
    private Role role;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
