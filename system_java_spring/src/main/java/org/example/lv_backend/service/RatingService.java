package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.util.SecurityUtil;
import org.example.lv_backend.dto.request.rating.RatingRequest;
import org.example.lv_backend.dto.response.rating.RatingResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Rating;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.RatingMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.RatingRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;
    private final SecurityUtil securityUtil;

    @Transactional
    public RatingResponse createRating(Long bookId, RatingRequest request) {
        String username = securityUtil.getCurrentUsername();
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (ratingRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new AppException(ErrorCode.REVIEW_EXISTED);
        }

        Rating rating = ratingMapper.toRating(request);
        rating.setUser(user);
        rating.setBook(book);
        rating.setCreatedAt(LocalDateTime.now());

        rating = ratingRepository.save(rating);
        recalculateAverageRating(book);

        return ratingMapper.toRatingResponse(rating);
    }

    @Transactional
    public RatingResponse updateRating(Long ratingId, RatingRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại"));
//chưa bỏ error code
        verifyRatingOwner(rating);

        ratingMapper.updateRating(rating, request);
        rating = ratingRepository.save(rating);

        recalculateAverageRating(rating.getBook());

        return ratingMapper.toRatingResponse(rating);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại"));
//chưa bỏ error code
        verifyRatingOwner(rating);

        Book book = rating.getBook();
        ratingRepository.delete(rating);

        recalculateAverageRating(book);
    }

    public Page<RatingResponse> getRatingsByBook(Long bookId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        Page<Rating> ratings = ratingRepository.findByBookId(bookId, pageable);
        return ratings.map(ratingMapper::toRatingResponse);
    }

    private void verifyRatingOwner(Rating rating) {
        String username = securityUtil.getCurrentUsername();
        boolean isAdmin = securityUtil.isAdmin();
        if (!isAdmin && (rating.getUser() == null || !username.equals(rating.getUser().getName()))) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void recalculateAverageRating(Book book) {
        Double avg = ratingRepository.findAverageRatingByBookId(book.getId());
        if (avg == null) {
            book.setAverageRating(BigDecimal.ZERO);
        } else {
            book.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }
        bookRepository.save(book);
    }


}
