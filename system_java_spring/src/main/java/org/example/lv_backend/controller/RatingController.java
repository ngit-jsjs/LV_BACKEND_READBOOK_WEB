package org.example.lv_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.rating.RatingRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.rating.RatingResponse;
import org.example.lv_backend.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PostMapping("/book/{bookId}")
    public ApiResponse<RatingResponse> createRating(
            @PathVariable Long bookId,
            @RequestBody @Valid RatingRequest request) {
        return ApiResponse.<RatingResponse>builder()
                .result(ratingService.createRating(bookId, request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @PutMapping("/{ratingId}")
    public ApiResponse<RatingResponse> updateRating(
            @PathVariable Long ratingId,
            @RequestBody @Valid RatingRequest request) {
        return ApiResponse.<RatingResponse>builder()
                .result(ratingService.updateRating(ratingId, request))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{ratingId}")
    public ApiResponse<String> deleteRating(@PathVariable Long ratingId) {
        ratingService.deleteRating(ratingId);
        return ApiResponse.<String>builder()
                .result("Rating has been deleted successfully")
                .build();
    }

    @GetMapping("/book/{bookId}")
    public ApiResponse<Page<RatingResponse>> getRatingsByBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.<Page<RatingResponse>>builder()
                .result(ratingService.getRatingsByBook(bookId, pageable))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    @GetMapping("/my-ratings")
    public ApiResponse<Page<RatingResponse>> getMyRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.<Page<RatingResponse>>builder()
                .result(ratingService.getMyRatings(pageable))
                .build();
    }
}
