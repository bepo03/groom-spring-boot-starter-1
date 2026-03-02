package com.study.profile_stack_api.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이면 수정하지 않음
        if (value == null) return true;

        // 값이 있지만 공백만 있는 문자열 false
        return !value.trim().isEmpty();
    }
}
