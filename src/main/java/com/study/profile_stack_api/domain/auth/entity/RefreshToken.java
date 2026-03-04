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
public class RefreshToken {
    private String refreshToken;
    private LocalDateTime refreshTokenExpiryDate;
}
