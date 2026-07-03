package org.example.lv_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.chapter.ChapterUnlockResponse;
import org.example.lv_backend.service.ChapterUnlockService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterUnlockController {

    private final ChapterUnlockService chapterUnlockService;

    @GetMapping("/my-unlocks")
    public ApiResponse<Page<ChapterUnlockResponse>> getMyUnlockHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ChapterUnlockResponse>>builder()
                .message("Lấy lịch sử mở khóa chương sách thành công")
                .result(chapterUnlockService.getMyUnlockHistory(page, size))
                .build();
    }

    @GetMapping("/admin/user/{userId}/unlocks")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ApiResponse<Page<ChapterUnlockResponse>> getUnlocksByUserAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ChapterUnlockResponse>>builder()
                .message("Lấy lịch sử mở khóa của người dùng thành công")
                .result(chapterUnlockService.getUnlocksByUserAdmin(userId, page, size))
                .build();
    }
}
