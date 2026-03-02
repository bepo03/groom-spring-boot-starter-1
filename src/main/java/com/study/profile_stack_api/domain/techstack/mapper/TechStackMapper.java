package com.study.profile_stack_api.domain.techstack.mapper;

import com.study.profile_stack_api.domain.techstack.dto.request.TechStackCreateRequest;
import com.study.profile_stack_api.domain.techstack.dto.request.TechStackUpdateRequest;
import com.study.profile_stack_api.domain.techstack.dto.response.TechStackResponse;
import com.study.profile_stack_api.domain.techstack.entity.TechStack;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechStackMapper {
    TechStack toEntity(TechStackCreateRequest request);

    @Mapping(target = "categoryIcon", expression = "java(techStack.getCategory().getIcon())")
    @Mapping(target = "proficiencyIcon", expression = "java(techStack.getProficiency().getIcon())")
    TechStackResponse toResponse(TechStack techStack);

    List<TechStackResponse> toResponseList(List<TechStack> techStacks);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TechStackUpdateRequest request, @MappingTarget TechStack techStack);
}
