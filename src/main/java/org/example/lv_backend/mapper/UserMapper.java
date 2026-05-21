package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.UserCreationRequest;
import org.example.lv_backend.dto.response.UserResponse;
import org.example.lv_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);
    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);
}
