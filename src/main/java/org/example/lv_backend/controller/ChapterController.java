package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.chapter.ChapterBatchUpdateRequest;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.chapter.ChapterDetailResponse;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.service.ChapterService;
import org.example.lv_backend.service.ChapterUnlockService;
//import org.example.lv_backend.service.EpubImportService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterUnlockService chapterUnlockService;
//    private final EpubImportService epubImportService;



    @GetMapping("/book/{bookId}")
    public ApiResponse<Page<ChapterResponse>> getChaptersByBookId(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ChapterResponse>>builder()
                .result(chapterService.getChaptersByBookId(bookId, page, size))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChapterDetailResponse> getChapterById(@PathVariable Long id) {
        return ApiResponse.<ChapterDetailResponse>builder()
                .result(chapterService.getChapterById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ApiResponse<ChapterResponse> updateChapter(@PathVariable Long id, @RequestBody @Valid ChapterUpdateRequest request) {
        return ApiResponse.<ChapterResponse>builder()
                .result(chapterService.updateChapter(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ApiResponse<String> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ApiResponse.<String>builder()
                .result("Chapter has been deleted")
                .build();
    }

    @PostMapping("/{id}/unlock")
    public ApiResponse<String> unlockChapter(@PathVariable Long id) {
        chapterUnlockService.unlockChapter(id);
        return ApiResponse.<String>builder()
                .result("Chapter has been unlocked successfully")
                .build();
    }

    @PutMapping("/book/{bookId}/batch")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ApiResponse<List<ChapterResponse>> batchUpdateChapters(
            @PathVariable Long bookId,
            @RequestBody ChapterBatchUpdateRequest request) {
        return ApiResponse.<List<ChapterResponse>>builder()
                .result(chapterService.batchUpdateChapters(bookId, request))
                .build();
    }

//    @PostMapping("/book/{bookId}")
//    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
//    public ApiResponse<ChapterResponse> createChapter(
//            @PathVariable Long bookId,
//            @RequestBody @Valid ChapterCreationRequest request) {
//        return ApiResponse.<ChapterResponse>builder()
//                .result(chapterService.createChapter(bookId, request))
//                .build();
//    }


    @DeleteMapping("/book/{bookId}/all")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public ApiResponse<String> deleteAllChapters(
            @PathVariable Long bookId) {
        chapterService.deleteAllChaptersByBook(bookId);
        return ApiResponse.<String>builder()
                .result("All chapters of this book have been deleted successfully")
                .build();
    }
}
