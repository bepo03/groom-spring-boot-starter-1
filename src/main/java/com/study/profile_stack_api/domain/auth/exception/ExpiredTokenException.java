package com.study.profile_stack_api.domain.auth.exception;

import com.study.profile_stack_api.global.exception.AuthException;
import com.study.profile_stack_api.global.exception.ErrorCode;

/**
 * JWT가 만료되었을 때 발생하는 예외
 */
public class ExpiredTokenException extends AuthException {

    public ExpiredTokenException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }
}
