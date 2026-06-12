package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.book.BookCreationRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BookResponse> createBook(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("request") @Valid BookCreationRequest request) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.createBook(request, file))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping(value = "/{bookId}/import-epub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BookResponse> importEpub(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.importEpub(bookId, file))
                .build();
    }
    
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @GetMapping("/my-upload-books")
    public ApiResponse<Page<BookResponse>> getMyUploadBook(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getMyUploadBook(keyword, page, size))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getAllPublishedBooks(page, size))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<BookResponse>> searchBook(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.searchBook(keyword, page, size))
                .build();
    }

    @GetMapping("/{bookId}")
    public ApiResponse<BookResponse> getBookById(@PathVariable("bookId") Long bookId) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.getBookById(bookId))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BookResponse> updateBook(@PathVariable("bookId") Long bookId,
                                                @RequestPart(value = "file", required = false) MultipartFile file,
                                                @RequestPart("request") @Valid BookCreationRequest request)
    {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.updateBook(bookId, request, file))
                .build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{bookId}")
    public ApiResponse<String> deleteBook(@PathVariable("bookId") Long bookId) {
        bookService.deleteBook(bookId);
        return ApiResponse.<String>builder()
                .result("Book has been deleted successfully")
                .build();
    }

}
