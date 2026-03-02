package com.study.profile_stack_api.domain.techstack.dto.request;

import com.study.profile_stack_api.global.validation.NotBlankIfPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 기술 스택 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechStackUpdateRequest {

    @NotBlankIfPresent(message = "기술명은 빈 값일 수 없습니다.")
    @Size(max = 50, message = "기술명은 50자를 초과할 수 없습니다.")
    private String name;

    @NotBlankIfPresent(message = "기술 카테고리는 빈 값일 수 없습니다.")
    private String category;

    @NotBlankIfPresent(message = "숙련도는 빈 값일 수 없습니다.")
    private String proficiency;

    @Min(value = 0, message = "사용 경험은 0년 이상이어야 합니다.")
    private Integer yearsOfExp;
}
