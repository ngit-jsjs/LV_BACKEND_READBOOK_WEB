package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.user.UserCreationRequest;
import org.example.lv_backend.dto.request.user.UserUpdateRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.auth.AuthenticationResponse;
import org.example.lv_backend.dto.response.user.SearchingUserResponse;
import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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


    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @GetMapping("/search")
    public ApiResponse<Page<SearchingUserResponse>> searchUser(@RequestParam(required = false, defaultValue = "") String keyword,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size){

        ApiResponse<SearchingUserResponse> apiResponse = new ApiResponse<>();

        Page<SearchingUserResponse> responses =
                userService.searchUser(keyword,page,size);

        return apiResponse.<Page<SearchingUserResponse>>builder()
                .result(responses)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
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


    @GetMapping("/{userId}")
    public ApiResponse<SearchingUserResponse> getUserById(
            @PathVariable Long userId
    ){

        return ApiResponse.<SearchingUserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }


}
