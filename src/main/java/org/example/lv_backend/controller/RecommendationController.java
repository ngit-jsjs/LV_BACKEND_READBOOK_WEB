package org.example.lv_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.repository.UserRepository;
import org.example.lv_backend.service.RecommendationService;
import org.example.lv_backend.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;


    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    @GetMapping
    public ApiResponse<List<BookResponse>> getRecommendations() {
        String username = securityUtil.getCurrentUsername();
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<BookResponse> responses = recommendationService.getRecommendationsForUser(user.getId());
        
        return ApiResponse.<List<BookResponse>>builder()
                .result(responses)
                .build();
    }


    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/train")
    public ApiResponse<String> triggerTraining() {
        log.info("Admin initiated recommendation training trigger.");
        String result = recommendationService.triggerRecommenderCalculation();
        
        return ApiResponse.<String>builder()
                .result(result)
                .build();
    }
}
