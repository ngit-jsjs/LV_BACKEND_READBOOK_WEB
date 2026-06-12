package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.SecurityUtil;
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
import org.springframework.transaction.annotation.Transactional;

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
            response.setBookAuthor(history.getBook().getAuthor());
            response.setCoverImageUrl(history.getBook().getCoverImageUrl());
        }
        if (history.getLastReadChapter() != null) {
            response.setLastChapterId(history.getLastReadChapter().getId());
            response.setLastChapterNumber(Long.valueOf(history.getLastReadChapter().getChapterNumber()));
            response.setLastChapterTitle(history.getLastReadChapter().getTitle());
        }
        return response;
    }



    public ReadingHistoryResponse saveOrUpdate(ReadingHistoryRequest request) {
        String username = securityUtil.getCurrentUsername();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        ReadingHistory history = readingHistoryRepository
                .findByUser_NameAndBook_Id(username, request.getBookId())
                .orElseGet(() -> {
                    User user = userRepository.findByName(username)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    return ReadingHistory.builder()
                            .user(user)
                            .book(book)
                            .build();
                });

        history.setLastReadChapter(chapter);

        return mapToResponse(readingHistoryRepository.save(history));
    }


    public Page<ReadingHistoryResponse> getMyReadingHistory(int page, int size) {
        String username = securityUtil.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<ReadingHistory> historyPage = readingHistoryRepository.findByUser_Name(username, pageable);
        return historyPage.map(this::mapToResponse);
    }
}
