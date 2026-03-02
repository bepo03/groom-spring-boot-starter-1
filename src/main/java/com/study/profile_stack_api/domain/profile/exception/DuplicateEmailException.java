package com.study.profile_stack_api.domain.profile.exception;

import com.study.profile_stack_api.global.exception.BusinessException;
import com.study.profile_stack_api.global.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super(
                ErrorCode.DUPLICATE_EMAIL,
                "이미 사용 중인 이메일입니다. (email: " + email + ")"
        );
    }
}
