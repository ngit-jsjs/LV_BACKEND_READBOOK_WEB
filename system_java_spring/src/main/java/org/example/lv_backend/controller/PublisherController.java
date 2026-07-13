package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.publisher.PublisherCreationRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.publisher.PublisherResponse;
import org.example.lv_backend.service.PublisherService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<PublisherResponse> createPublisher(@RequestBody @Valid PublisherCreationRequest request) {
        return ApiResponse.<PublisherResponse>builder()
                .result(publisherService.createPublisher(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<PublisherResponse>> getAllPublishers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<PublisherResponse>>builder()
                .result(publisherService.getAllPublishers(page, size))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<PublisherResponse>> searchPublisher(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<PublisherResponse>>builder()
                .result(publisherService.searchPublishers(keyword, page, size))
                .build();
    }

//    @GetMapping("/{id}")
//    public ApiResponse<PublisherResponse> getPublisherById(@PathVariable Long id) {
//        return ApiResponse.<PublisherResponse>builder()
//                .result(publisherService.getPublisherById(id))
//                .build();
//    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<PublisherResponse> updatePublisher(
            @PathVariable Long id,
            @RequestBody @Valid PublisherCreationRequest request) {
        return ApiResponse.<PublisherResponse>builder()
                .result(publisherService.updatePublisher(id, request))
                .build();
    }

}
