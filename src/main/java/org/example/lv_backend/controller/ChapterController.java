package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.service.ChapterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping
    @PreAuthorize("hasAuthority('AUTHOR') or hasAuthority('ADMIN')")
    public ApiResponse<ChapterResponse> createChapter(@RequestBody @Valid ChapterCreationRequest request) {
        return ApiResponse.<ChapterResponse>builder()
                .result(chapterService.createChapter(request))
                .build();
    }

    @GetMapping("/book/{bookId}")
    public ApiResponse<List<ChapterResponse>> getChaptersByBookId(@PathVariable Long bookId) {
        return ApiResponse.<List<ChapterResponse>>builder()
                .result(chapterService.getChaptersByBookId(bookId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChapterResponse> getChapterById(@PathVariable Long id) {
        return ApiResponse.<ChapterResponse>builder()
                .result(chapterService.getChapterById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTHOR') or hasAuthority('ADMIN')")
    public ApiResponse<ChapterResponse> updateChapter(@PathVariable Long id, @RequestBody @Valid ChapterUpdateRequest request) {
        return ApiResponse.<ChapterResponse>builder()
                .result(chapterService.updateChapter(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTHOR') or hasAuthority('ADMIN')")
    public ApiResponse<String> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ApiResponse.<String>builder()
                .result("Chapter has been deleted")
                .build();
    }
}
