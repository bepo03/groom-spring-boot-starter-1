package com.study.profile_stack_api.domain.profile.exception;

import com.study.profile_stack_api.global.exception.BusinessException;
import com.study.profile_stack_api.global.exception.ErrorCode;

public class ProfileNotFoundException extends BusinessException {
    public ProfileNotFoundException(Long id) {
        super(
                ErrorCode.PROFILE_NOT_FOUND,
                "해당 프로필을 찾을 수 없습니다. (id: " + id + ")"
        );
    }
}
