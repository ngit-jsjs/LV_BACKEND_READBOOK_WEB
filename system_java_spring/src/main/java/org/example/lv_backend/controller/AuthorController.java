package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.author.AuthorCreationRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.author.AuthorResponse;
import org.example.lv_backend.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<AuthorResponse> createAuthor(@RequestBody @Valid AuthorCreationRequest request) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.createAuthor(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<AuthorResponse>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<AuthorResponse>>builder()
                .result(authorService.getAllAuthors(page, size))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<AuthorResponse>> searchAuthor(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<AuthorResponse>>builder()
                .result(authorService.searchAuthors(keyword, page, size))
                .build();
    }

//    @GetMapping("/{id}")
//    public ApiResponse<AuthorResponse> getAuthorById(@PathVariable Long id) {
//        return ApiResponse.<AuthorResponse>builder()
//                .result(authorService.getAuthorById(id))
//                .build();
//    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<AuthorResponse> updateAuthor(
            @PathVariable Long id,
            @RequestBody @Valid AuthorCreationRequest request) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.updateAuthor(id, request))
                .build();
    }

}
