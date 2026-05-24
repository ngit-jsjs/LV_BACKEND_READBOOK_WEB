package org.example.lv_backend.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.user.UserCreationRequest;
import org.example.lv_backend.dto.request.user.UserUpdateRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.user.SearchingAuthorResponse;
import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<UserResponse> apiResponse=new ApiResponse<>();
        UserResponse userResponse=userService.createUser(request);
        apiResponse.setResult(userResponse);
        return apiResponse;
    }


    @GetMapping("/search")
    public ApiResponse<Page<SearchingAuthorResponse>> searchAuthor(@RequestParam String keyword,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int size){

        ApiResponse<SearchingAuthorResponse> apiResponse = new ApiResponse<>();

        Page<SearchingAuthorResponse> responses =
                userService.searchAuthor(keyword,page,size);

        return apiResponse.<Page<SearchingAuthorResponse>>builder()
                .result(responses)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_AUTHOR', 'SCOPE_USER')")
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PreAuthorize("""
    hasAuthority('SCOPE_ADMIN')
    or
    #userId == authentication.principal.claims['userId']
    """)
    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequest request){
        return userService.updateUser(userId,request);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return "User has been delete";
    }





}
