package com.study.profile_stack_api.domain.auth.mapper;

import com.study.profile_stack_api.domain.auth.dto.request.SignupRequest;
import com.study.profile_stack_api.domain.auth.dto.response.SignupResponse;
import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", imports = Role.class)
public interface AuthMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "password", source = "encodedPassword"),
            @Mapping(target = "role", expression = "java(Role.USER)"),
            @Mapping(target = "enabled", constant = "true"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    Member toEntity(SignupRequest request, String encodedPassword);

    @Mappings({
            @Mapping(target = "userId", source = "id"),
            @Mapping(target = "message", constant = "회원가입이 성공적으로 완료되었습니다.")
    })
    SignupResponse toResponse(Member member);
}
