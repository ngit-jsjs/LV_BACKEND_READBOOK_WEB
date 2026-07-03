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

/**
 * REST Controller cung cấp API quản lý lịch sử đọc sách.
 * - PUT /reading-history: Ghi nhớ/cập nhật lịch sử đọc (upsert)
 * - GET /reading-history: Lấy danh sách lịch sử đọc có phân trang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reading-history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    /**
     * API ghi nhớ hoặc cập nhật lịch sử đọc sách.
     * Frontend gọi API này mỗi khi user mở đọc 1 chương.
     * Nếu chưa có bản ghi cho sách này -> tạo mới.
     * Nếu đã có -> cập nhật chương đọc cuối và thời gian.
     *
     * @param request chứa bookId và chapterId.
     * @return Thông tin lịch sử đọc sau khi lưu.
     */
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PutMapping
    public ApiResponse<ReadingHistoryResponse> saveOrUpdate(
            @RequestBody @Valid ReadingHistoryRequest request) {
        return ApiResponse.<ReadingHistoryResponse>builder()
                .result(readingHistoryService.saveOrUpdate(request))
                .build();
    }

    /**
     * API lấy danh sách lịch sử đọc của user hiện tại có hỗ trợ phân trang.
     * Sắp xếp theo thời gian đọc lần cuối giảm dần (đọc gần nhất hiển thị trước).
     *
     * @param page chỉ số trang (mặc định 0).
     * @param size số bản ghi trên mỗi trang (mặc định 10).
     * @return Page chứa danh sách ReadingHistoryResponse.
     */
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
