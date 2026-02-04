package com.problemservice.mapper;

import com.problemservice.dto.CommentDTO;
import com.problemservice.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "parent.id", target = "parentId")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}
