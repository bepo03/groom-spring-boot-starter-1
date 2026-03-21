package com.study.profile_stack_api.domain.profile.mapper;

import com.study.profile_stack_api.domain.profile.dto.request.ProfileCreateRequest;
import com.study.profile_stack_api.domain.profile.dto.request.ProfileUpdateRequest;
import com.study.profile_stack_api.domain.profile.dto.response.ProfileResponse;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "member", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    Profile toEntity(ProfileCreateRequest request);

    @Mappings({
            @Mapping(target = "memberId", source = "member.id"),
            @Mapping(target = "position", source = "position"),
            @Mapping(target = "positionIcon", expression = "java(profile.getPosition().getIcon())")
    })
    ProfileResponse toResponse(Profile profile);

    List<ProfileResponse> toResponseList(List<Profile> profiles);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "member", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    void updateEntity(ProfileUpdateRequest request, @MappingTarget Profile profile);
}
