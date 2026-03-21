package com.study.profile_stack_api.domain.profile.validation;

import com.study.profile_stack_api.domain.profile.repository.ProfileRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailIfPresentValidator implements ConstraintValidator<UniqueEmailIfPresent, String> {

    private final ProfileRepository profileRepository;

    /**
     * 이메일 중복 검증기 생성
     *
     * @param profileRepository 프로필 조회 Repository
     */
    public UniqueEmailIfPresentValidator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * 값이 존재할 때만 이메일 중복 여부를 검증
     *
     * @param value 검증할 이메일 값
     * @param context 검증 컨텍스트
     * @return 검증 통과 여부
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이면 수정 안 함
        if (value == null) return true;

        // 공백이면 수정 안 함
        if (value.trim().isEmpty()) return true;

        // 존재 하면 실패
        return !profileRepository.existsByEmailIgnoreCase(value);
    }
}
