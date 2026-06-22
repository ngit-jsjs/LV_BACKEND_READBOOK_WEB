package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.SecurityUtil;
import org.example.lv_backend.dto.request.chapter.ChapterBatchUpdateRequest;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.dto.response.chapter.ChapterDetailResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.ChapterMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.ChapterRepository;
import org.example.lv_backend.repository.ChapterUnlockRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ChapterUnlockRepository chapterUnlockRepository;
    private final SecurityUtil securityUtil;
    private final EpubParserService epubParserService;



    @Transactional(readOnly = true)
    public Page<ChapterResponse> getChaptersByBookId(Long bookId, int page, int size) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));


        if (!securityUtil.isAdmin() && book.getStatus() == BookStatus.UNAVAILABLE) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<Chapter> chapters = chapterRepository.findByBookIdOrderByChapterNumberAsc(bookId, pageable);

        Set<Long> unlockedChapterIds = getUnlockedChapterIds(chapters);

        List<ChapterResponse> responseList = new ArrayList<>();

        for (Chapter chapter : chapters) {
            boolean isLocked = !securityUtil.isAdmin()
                    && !Boolean.TRUE.equals(chapter.getIsFree())
                    && !unlockedChapterIds.contains(chapter.getId());

            responseList.add(chapterMapper.toChapterResponse(chapter, isLocked));
        }

        return new PageImpl<>(responseList, pageable, chapters.getTotalElements());
    }


    private boolean isChapterLocked(Chapter chapter) {
                if (securityUtil.isAdmin() ||chapter.getIsFree()) {
                    return false;
                }
                String currentUsername = securityUtil.getCurrentUsername();
                if (currentUsername == null) {
                    return true;
                }
                User currentUser = userRepository.findByName(currentUsername).orElse(null);
                if (currentUser != null) {
                    boolean isUnlocked = chapterUnlockRepository.checkIfUnlocked(currentUser.getId(), chapter.getId());
                    return !isUnlocked;
                } else {
                    return true;
                }
            }



    @Transactional(readOnly = true)
    public ChapterDetailResponse getChapterById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));


        if (!securityUtil.isAdmin()) {
            if (chapter.getBook().getStatus() == BookStatus.UNAVAILABLE) {
                {
                    throw new AppException(ErrorCode.CHAPTER_NOT_EXISTED);
                }
            }
        }

        boolean isLocked = isChapterLocked(chapter);

        ChapterDetailResponse response = chapterMapper.toChapterDetailResponse(chapter, isLocked);
        if (!isLocked) {
            String content = epubParserService.readChapterContent(
                    chapter.getBook().getStoragePath(),
                    chapter.getSectionIndex(),
                    chapter.getFragmentId(),
                    chapter.getNextAnchor()
            );
            response.setContent(content);
        }
        return response;

    }




    private Set<Long> getUnlockedChapterIds(Page<Chapter> chapters) {
        if (securityUtil.isAdmin()) {
            return Collections.emptySet();
        }
        String currentUsername = securityUtil.getCurrentUsername();
        if (currentUsername == null) {
            return Collections.emptySet();
        }
        List<Long> chapterIds = chapters.stream().map(Chapter::getId).collect(Collectors.toList());
        if (chapterIds.isEmpty()) {
            return Collections.emptySet();
        }
        User currentUser = userRepository.findByName(currentUsername).orElse(null);
        if (currentUser != null) {
            return chapterUnlockRepository.findUnlockedChapterIds(currentUser.getId(), chapterIds);
        } else {
            return Collections.emptySet();
        }
    }




//
    @Transactional
    public ChapterResponse updateChapter(Long id, ChapterUpdateRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));


        if (chapterRepository.existsByBookIdAndChapterNumberAndIdNot(chapter.getBook().getId(), request.getChapterNumber(), id)) {
            throw new AppException(ErrorCode.CHAPTER_NUMBER_EXISTED);
        }

        chapterMapper.updateChapter(chapter, request);

        if (request.getIsFree()) {
            chapter.markAsFree();
        } else {
            BigDecimal price = request.getPrice();

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }
            chapter.setPrice(price);
            chapter.setIsFree(false);
        }

        return chapterMapper.toChapterResponse(chapterRepository.save(chapter),false);
    }



    @Transactional
    public void deleteChapter(Long id) {
        if (!chapterRepository.existsById(id)) {
            throw new AppException(ErrorCode.CHAPTER_NOT_EXISTED);
        }

        chapterRepository.deleteById(id);
    }


    @Transactional
    public List<ChapterResponse> batchUpdateChapters(Long bookId, ChapterBatchUpdateRequest request) {
        if (!bookRepository.existsById(bookId)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }

        List<Chapter> chaptersToUpdate = chapterRepository.findAllById(request.getChapterIds());

            for (Chapter chapter : chaptersToUpdate) {
                    if (Boolean.TRUE.equals(request.getIsFree())) {
                        chapter.setIsFree(true);
                    } else {
                        BigDecimal price = request.getPrice();
                        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
                        {
                                throw new AppException(ErrorCode.INVALID_PRICE);
                            }
                            chapter.setPrice(price);
                            chapter.setIsFree(false);
                        } 
                       
                    }

            List<Chapter> savedChapters = chapterRepository.saveAll(chaptersToUpdate);
            return savedChapters.stream()
                    .map(chapter -> chapterMapper.toChapterResponse(chapter, false))
                    .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllChaptersByBook(Long bookId) {
        Book book =bookRepository.findById(bookId).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        List<Chapter> chaptersToDelete = book.getChapters();

        chapterRepository.deleteAll(chaptersToDelete);
    }


    @Transactional
    public void deleteSelectedChapters(Long bookId, List<Long> chapterIds) {

        if (!bookRepository.existsById(bookId)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }

        List<Chapter> chaptersToDelete = chapterRepository.findAllById(chapterIds);


        chapterRepository.deleteAll(chaptersToDelete);
    }


}
