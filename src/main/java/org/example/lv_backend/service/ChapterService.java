package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.ChapterMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.ChapterRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;
    private final BookRepository bookRepository;

    private void verifyOwnership(Book book) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!book.getUser().getName().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOK);
        }
    }



    public ChapterResponse createChapter(ChapterCreationRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        verifyOwnership(book);

        if (chapterRepository.existsByBookIdAndChapterNumber(request.getBookId(), request.getChapterNumber())) {
            throw new AppException(ErrorCode.CHAPTER_NUMBER_EXISTED);
        }

        Chapter chapter = chapterMapper.toChapter(request);
        chapter.setBook(book);

        if (Boolean.TRUE.equals(request.getIsFree())) {
            chapter.markAsFree();


        } else {
            chapter.setPrice(request.getPrice());
        }

        return chapterMapper.toChapterResponse(chapterRepository.save(chapter));
    }




    public List<ChapterResponse> getChaptersByBookId(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        
        List<Chapter> chapters = chapterRepository.findByBookIdOrderByChapterNumberAsc(bookId);
        return chapters.stream()
                .map(chapterMapper::toChapterResponse)
                .collect(Collectors.toList());
    }



    public ChapterResponse getChapterById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));


        return chapterMapper.toChapterResponse(chapter);
    }



    public ChapterResponse updateChapter(Long id, ChapterUpdateRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        verifyOwnership(chapter.getBook());

        if (chapterRepository.existsByBookIdAndChapterNumberAndIdNot(chapter.getBook().getId(), request.getChapterNumber(), id)) {
            throw new AppException(ErrorCode.CHAPTER_NUMBER_EXISTED);
        }

        chapterMapper.updateChapter(chapter, request);

        if (Boolean.TRUE.equals(request.getIsFree())) {
            chapter.markAsFree();
        }

        return chapterMapper.toChapterResponse(chapterRepository.save(chapter));
    }



    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        verifyOwnership(chapter.getBook());

        chapterRepository.deleteById(id);
    }
}
