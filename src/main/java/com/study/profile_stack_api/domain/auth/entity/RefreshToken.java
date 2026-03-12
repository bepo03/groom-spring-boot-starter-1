package com.study.profile_stack_api.domain.auth.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh 토큰 Entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class RefreshToken {
    private Long id;
    private Long member_id;
    private String token;
    private LocalDateTime expiry_date;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
