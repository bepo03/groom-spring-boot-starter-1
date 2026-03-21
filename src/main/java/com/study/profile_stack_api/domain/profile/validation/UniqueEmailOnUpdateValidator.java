package com.study.profile_stack_api.domain.profile.validation;

import com.study.profile_stack_api.domain.profile.dto.request.ProfileUpdateRequest;
import com.study.profile_stack_api.domain.profile.repository.ProfileRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.springframework.stereotype.Component;

@Component
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class UniqueEmailOnUpdateValidator implements ConstraintValidator<UniqueEmailOnUpdate, Object[]> {

    private final ProfileRepository profileRepository;

    /**
     * 수정 요청용 이메일 중복 검증기 생성
     *
     * @param profileRepository 프로필 조회 Repository
     */
    public UniqueEmailOnUpdateValidator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * 수정 대상 ID를 제외하고 이메일 중복 여부를 검증
     *
     * @param values 검증 대상 파라미터 배열
     * @param context 검증 컨텍스트
     * @return 검증 통과 여부
     */
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
        return !profileRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }
}
