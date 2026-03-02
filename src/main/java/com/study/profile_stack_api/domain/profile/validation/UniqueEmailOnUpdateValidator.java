package com.study.profile_stack_api.domain.profile.validation;

import com.study.profile_stack_api.domain.profile.dao.ProfileDao;
import com.study.profile_stack_api.domain.profile.dto.request.ProfileUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.springframework.stereotype.Component;

@Component
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class UniqueEmailOnUpdateValidator implements ConstraintValidator<UniqueEmailOnUpdate, Object[]> {

    private final ProfileDao profileDao;

    public UniqueEmailOnUpdateValidator(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        // 값이 없거나 파라미터가 2개 미만이면 검증 통과
        if (values == null || values.length < 2) {
            return true;
        }

        // 첫 번째 값이 Long(id)인지,
        // 두 번째 값이 ProfileUpdateRequest인지 확인 후 아니면 검증 통과
        // 패턴 매칭 instanceof 사용 (타입 체크 + 변수 선언)
        if (!(values[0] instanceof Long id) || !(values[1] instanceof ProfileUpdateRequest request)) {
            return true;
        }

        // 이메일이 null이거나 공백이면 검증 통과
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        // DB에 "자기 자신(id 제외) + 동일 이메일" 이 존재하면 검증 실패
        return !profileDao.existsByEmailAndIdNot(id, email);
    }
}
