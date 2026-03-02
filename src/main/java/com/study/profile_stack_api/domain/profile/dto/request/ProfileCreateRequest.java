package com.study.profile_stack_api.domain.profile.dto.request;

import com.study.profile_stack_api.global.validation.NotBlankIfPresent;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 프로필 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCreateRequest {
    @NotBlankIfPresent(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    private String name;

    @NotBlankIfPresent(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
    private String email;

    @Size(max = 500, message = "자기소개는 500자를 초과할 수 없습니다.")
    private String bio;

    @NotNull(message = "직무는 필수입니다.")
    private String position;

    @NotNull(message = "경력은 필수입니다.")
    @Min(value = 0, message = "경력은 0년 이상이어야 합니다.")
    private Integer careerYears;

    @Size(max = 200, message = "GitHub 주소는 200자를 초과할 수 없습니다.")
    private String githubUrl;

    @Size(max = 200, message = "블로그 주소는 200자를 초과할 수 없습니다.")
    private String blogUrl;
}
