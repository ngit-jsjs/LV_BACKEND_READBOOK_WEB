package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.chapter.ChapterListResponse;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.service.ChapterService;
import org.example.lv_backend.service.ChapterUnlockService;
//import org.example.lv_backend.service.EpubImportService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterUnlockService chapterUnlockService;
//    private final EpubImportService epubImportService;



    @GetMapping("/book/{bookId}")
    public ApiResponse<Page<ChapterListResponse>> getChaptersByBookId(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ChapterListResponse>>builder()
                .result(chapterService.getChaptersByBookId(bookId, page, size))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChapterResponse> getChapterById(@PathVariable Long id) {
        return ApiResponse.<ChapterResponse>builder()
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


}
