package com.userservice.mapper;

import com.userservice.dto.request.UserRequest;
import com.userservice.dto.response.UserResponse;
import com.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    User toEntity(UserRequest request);

    void updateEntityFromRequest(UserRequest request, @MappingTarget User user);
}
