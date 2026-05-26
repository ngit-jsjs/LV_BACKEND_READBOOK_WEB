package org.example.lv_backend.controller;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.auth.AuthenticationRequest;
import org.example.lv_backend.dto.request.auth.IntrospectRequest;
import org.example.lv_backend.dto.request.auth.LogoutRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.auth.AuthenticationResponse;
import org.example.lv_backend.dto.response.auth.IntrospectResponse;
import org.example.lv_backend.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping ("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login (@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticationResponse(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect (@RequestBody IntrospectRequest request) throws ParseException, JOSEException
    {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout (@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
}
