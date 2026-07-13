package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.readinghistory.ReadingHistoryRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.readinghistory.ReadingHistoryResponse;
import org.example.lv_backend.service.ReadingHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reading-history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PutMapping
    public ApiResponse<ReadingHistoryResponse> saveOrUpdate(
            @RequestBody @Valid ReadingHistoryRequest request) {
        return ApiResponse.<ReadingHistoryResponse>builder()
                .result(readingHistoryService.saveOrUpdate(request))
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @GetMapping
    public ApiResponse<Page<ReadingHistoryResponse>> getMyReadingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReadingHistoryResponse>>builder()
                .result(readingHistoryService.getMyReadingHistory(page, size))
                .build();
    }
}
