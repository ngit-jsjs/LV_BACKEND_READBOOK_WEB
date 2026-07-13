package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.request.booklist.BookListRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.service.BookListService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/booklists")
public class BookListController {

    private final BookListService bookListService;


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PostMapping
    public ApiResponse<BookListResponse> createBookList(@RequestBody @Valid BookListRequest request) {
        return ApiResponse.<BookListResponse>builder()
                .result(bookListService.createBookList(request))
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @GetMapping
    public ApiResponse<Object> getMyBookLists(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            return ApiResponse.<Object>builder()
                    .result(bookListService.getMyBookLists(page, size))
                    .build();
        }
        return ApiResponse.<Object>builder()
                .result(bookListService.getMyBookLists())
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<BookListResponse> getBookListById(@PathVariable Long id) {
        return ApiResponse.<BookListResponse>builder()
                .result(bookListService.getBookListById(id))
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<BookListResponse> updateBookList(
            @PathVariable Long id,
            @RequestBody @Valid BookListRequest request) {
        return ApiResponse.<BookListResponse>builder()
                .result(bookListService.updateBookList(id, request))
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBookList(@PathVariable Long id) {
        bookListService.deleteBookList(id);
        return ApiResponse.<String>builder()
                .result("BookList has been deleted successfully")
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PostMapping("/{id}/books/{bookId}")
    public ApiResponse<BookListResponse> addBookToBookList(
            @PathVariable Long id,
            @PathVariable Long bookId) {
        return ApiResponse.<BookListResponse>builder()
                .result(bookListService.addBookToBookList(id, bookId))
                .build();
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @DeleteMapping("/{id}/books/{bookId}")
    public ApiResponse<BookListResponse> removeBookFromBookList(
            @PathVariable Long id,
            @PathVariable Long bookId) {
        return ApiResponse.<BookListResponse>builder()
                .result(bookListService.removeBookFromBookList(id, bookId))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @GetMapping("/{id}/books")
    public ApiResponse<Page<BookResponse>> getBooksInBookList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookListService.getBooksInBookList(id, page, size))
                .build();
    }
}
