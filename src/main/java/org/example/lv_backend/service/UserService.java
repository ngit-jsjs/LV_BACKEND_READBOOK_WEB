package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.UserCreationRequest;
import org.example.lv_backend.dto.response.UserResponse;
import org.example.lv_backend.entity.Role;
import org.example.lv_backend.entity.RoleName;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.UserMapper;
import org.example.lv_backend.repository.RoleRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    public UserResponse createUser (UserCreationRequest request){
        User user= userMapper.toUser(request);

        PasswordEncoder passwordEncoder =new BCryptPasswordEncoder(10);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByRoleName(RoleName.USER).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Set<Role> roles=new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));

    }

}
