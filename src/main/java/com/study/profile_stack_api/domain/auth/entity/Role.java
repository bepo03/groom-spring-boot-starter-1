package com.study.profile_stack_api.domain.auth.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 권한 Enum
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    USER("👑", "일반 사용자"),
    ADMIN("🙋", "관리자"),
    MANAGER("🔧", "매니저");

    private final String icon;
    private final String description;
}
