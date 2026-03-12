package com.study.profile_stack_api.global.exception;

import lombok.Getter;

/**
 * 인증/인가 처리 과정에서 발생하는 인증 관련 예외의 최상위 클래스
 */
@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthException(String message) {
        super(message);
        this.errorCode = ErrorCode.AUTH_ERROR;
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
