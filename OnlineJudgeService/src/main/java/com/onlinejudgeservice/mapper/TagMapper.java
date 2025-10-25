package com.onlinejudgeservice.mapper;

import com.onlinejudgeservice.dto.request.TagRequest;
import com.onlinejudgeservice.dto.response.TagResponse;
import com.onlinejudgeservice.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "problems", ignore = true)
    Tag toEntity(TagRequest request);

    TagResponse toResponse(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "problems", ignore = true)
    void updateEntity(TagRequest request, @MappingTarget Tag tag);
}