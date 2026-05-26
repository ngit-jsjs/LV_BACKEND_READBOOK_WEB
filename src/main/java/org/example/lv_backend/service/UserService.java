package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.Configuration;
import org.example.lv_backend.dto.request.user.UserCreationRequest;
import org.example.lv_backend.dto.request.user.UserUpdateRequest;
import org.example.lv_backend.dto.response.user.SearchingUserResponse;
import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.entity.Role;
import org.example.lv_backend.entity.RoleName;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.mapper.UserMapper;
import org.example.lv_backend.repository.RoleRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final RoleRepository roleRepository;
    private final Configuration configuration;


    public UserResponse createUser (UserCreationRequest request){

        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null
                && context.getAuthentication().isAuthenticated()
                && !(context.getAuthentication() instanceof AnonymousAuthenticationToken)) {
            throw new AppException(ErrorCode.ALREADY_AUTHENTICATED);
        }


        if(userRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.USER_EXISTED);
        if(userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);


        User user= userMapper.toUser(request);


        user.setPassword( configuration.passwordEncoder().encode(request.getPassword()));

        BigDecimal amount=BigDecimal.valueOf(100);
        user.setAmount(amount);

        Role role = roleRepository.findByRoleName(RoleName.USER).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));


        Set<Role> roles=new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        UserResponse response = userMapper.toUserResponse(userRepository.save(user));

        response.setRoles(
                user.getRoles()
                        .stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet())
        );

        return response;

    }


    public UserResponse getMyInfo(){
        var context= SecurityContextHolder.getContext();
        String name=context.getAuthentication().getName();

        User user=userRepository.findByName(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        UserResponse response = userMapper.toUserResponse(user);

        // Set roles
        response.setRoles(
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet())
        );

        response.setAmount(user.getAmount());


        return response;
    }


    public UserResponse updateUser(Long id, UserUpdateRequest request){
        User user=userRepository.findById(id).
                orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        if(request.getPassword()!=null){
            user.setPassword(configuration.passwordEncoder().encode(request.getPassword()));
        }
        return userMapper.toUserResponse(userRepository.save(user));

    }

//    public UserResponse updateAuthor(Long id, UserUpdateRequest request){
//        User user=userRepository.findById(id).
//                orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
//        userMapper.updateUser(user, request);
//
//        return userMapper.toUserResponse(userRepository.save(user));
//
//    }


    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }



    public Page<SearchingUserResponse> searchUser (String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users =
                userRepository
                        .findDistinctByNameContainingIgnoreCase(
                                keyword,
                                pageable
                        );

        return users.map(userMapper::toSearchingUserResponse);
    }





}
