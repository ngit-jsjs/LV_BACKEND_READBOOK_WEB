package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.user.UserCreationRequest;
import org.example.lv_backend.dto.response.user.SearchingUserResponse;
import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.dto.request.user.UserUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);
    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);

    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    SearchingUserResponse toSearchingUserResponse(User user);
}
