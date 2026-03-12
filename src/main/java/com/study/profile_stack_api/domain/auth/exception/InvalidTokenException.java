package com.study.profile_stack_api.domain.auth.exception;

import com.study.profile_stack_api.global.exception.AuthException;
import com.study.profile_stack_api.global.exception.ErrorCode;

/**
 * JWT 형식이 잘못되었거나 서명이 유효하지 않을 때 발생하는 예외
 */
public class InvalidTokenException extends AuthException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
