package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.util.SecurityUtil;
import org.example.lv_backend.dto.request.readinghistory.ReadingHistoryRequest;
import org.example.lv_backend.dto.response.readinghistory.ReadingHistoryResponse;
import org.example.lv_backend.entity.*;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.ReadingHistoryMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.ChapterRepository;
import org.example.lv_backend.repository.ReadingHistoryRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final ReadingHistoryMapper readingHistoryMapper;
    private final SecurityUtil securityUtil;


    private ReadingHistoryResponse mapToResponse(ReadingHistory history) {
        ReadingHistoryResponse response = readingHistoryMapper.toReadingHistoryResponse(history);
        if (history.getBook() != null) {
            response.setBookTitle(history.getBook().getTitle());
            response.setBookAuthor(history.getBook().getAuthor().getName());
            response.setCoverImageUrl(history.getBook().getCoverImageUrl());
        }
        if (history.getLastReadChapter() != null) {
            response.setLastChapterId(history.getLastReadChapter().getId());
            response.setLastChapterNumber(history.getLastReadChapter().getChapterNumber());
            response.setLastChapterTitle(history.getLastReadChapter().getTitle());
        }
        response.setIsCompleted(history.getIsCompleted());
        return response;
    }



    public ReadingHistoryResponse saveOrUpdate(ReadingHistoryRequest request) {
        Long userId = securityUtil.getCurrentUserId();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        ReadingHistory history = readingHistoryRepository
                .findByUser_IdAndBook_Id(userId, request.getBookId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    return ReadingHistory.builder()
                            .user(user)
                            .book(book)
                            .build();
                });

        history.setLastReadChapter(chapter);

        if (Boolean.TRUE.equals(request.getIsCompleted())) {
            history.setIsCompleted(true);
        } else {
            chapterRepository.findTopByBookIdOrderByChapterNumberDesc(book.getId())
                    .ifPresent(lastChap -> {
                        if (lastChap.getId().equals(chapter.getId())) {
                            history.setIsCompleted(true);
                        }
                    });
        }

        return mapToResponse(readingHistoryRepository.save(history));
    }


    public Page<ReadingHistoryResponse> getMyReadingHistory(int page, int size) {
        Long userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<ReadingHistory> historyPage = readingHistoryRepository.findByUser_Id(userId, pageable);
        return historyPage.map(this::mapToResponse);
    }
}
