package com.study.profile_stack_api.domain.auth.exception;

import com.study.profile_stack_api.global.exception.AuthException;
import com.study.profile_stack_api.global.exception.ErrorCode;

public class DuplicateResourceException extends AuthException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}
