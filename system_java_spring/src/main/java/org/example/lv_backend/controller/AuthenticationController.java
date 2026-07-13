package org.example.lv_backend.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.auth.AuthenticationRequest;
import org.example.lv_backend.dto.request.auth.IntrospectRequest;
import org.example.lv_backend.dto.request.auth.LogoutRequest;
import org.example.lv_backend.dto.request.auth.VerifyOtpRequest;
import org.example.lv_backend.dto.request.auth.ResetPasswordRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.auth.AuthenticationResponse;
import org.example.lv_backend.dto.response.auth.IntrospectResponse;
import org.example.lv_backend.service.auth.AuthenticationService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestBody VerifyOtpRequest request) {
        return authenticationService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    public ApiResponse<String> resendOtp(@RequestParam String email) {
        return authenticationService.resendOtp(email);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam String email) {
        return authenticationService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return authenticationService.resetPassword(request);
    }
}
