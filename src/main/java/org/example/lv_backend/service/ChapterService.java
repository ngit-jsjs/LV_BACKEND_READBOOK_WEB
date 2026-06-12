package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.SecurityUtil;
import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.chapter.ChapterListResponse;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.entity.Book;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private boolean isOwner(Book book) {
        if (securityUtil.isAdmin()) {
            return true;
        }
        String currentUsername = securityUtil.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }
        return book.getUser() != null && currentUsername.equals(book.getUser().getName());
    }



    private void verifyOwnership(Book book) {
        if (!isOwner(book)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOK);
        }
    }


    private boolean isChapterLocked(Chapter chapter, boolean hasFullAccess, Set<Long> unlockedChapterIds) {
        if (hasFullAccess || Boolean.TRUE.equals(chapter.getIsFree())) {
            return false;
        }
        return !unlockedChapterIds.contains(chapter.getId());
    }







    @Transactional(readOnly = true)
    public Page<ChapterListResponse> getChaptersByBookId(Long bookId, int page, int size) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        boolean hasFullAccess = isOwner(book);
        Pageable pageable = PageRequest.of(page, 1000);

        Page<Chapter> chapters = hasFullAccess
                ? chapterRepository.findByBookIdOrderByChapterNumberAsc(bookId, pageable)
                : chapterRepository.findByBookIdAndIsPublishedTrueOrderByChapterNumberAsc(bookId, pageable);

        Set<Long> unlockedChapterIds = getUnlockedChapterIds(chapters, hasFullAccess);

        return chapters.map(chapter -> 
        {
            boolean isLocked;

            if (hasFullAccess) {
                isLocked = false;
            }
            else if (Boolean.TRUE.equals(chapter.getIsFree())) {
                isLocked = false;
            }
            else if (unlockedChapterIds.contains(chapter.getId())) {
                isLocked = false;
            }
            else {
                isLocked = true;
            }

            return chapterMapper.toChapterListResponse(chapter, isLocked);
        });
    }


    private boolean isChapterLocked(Chapter chapter, boolean hasFullAccess) {
                if (hasFullAccess || Boolean.TRUE.equals(chapter.getIsFree())) {
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
    public ChapterResponse getChapterById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        boolean hasFullAccess = isOwner(chapter.getBook());

        if (!hasFullAccess && !Boolean.TRUE.equals(chapter.getIsPublished())) {
            throw new AppException(ErrorCode.CHAPTER_NOT_EXISTED);
        }

        boolean isLocked = isChapterLocked(chapter, hasFullAccess);

        ChapterResponse response = chapterMapper.toChapterDetailResponse(chapter, isLocked);
        if (!isLocked) {
            try {
                String content = epubParserService.readChapterContent(
                        chapter.getBook().getStoragePath(), 
                        chapter.getSectionIndex(),
                        chapter.getFragmentId(),
                        chapter.getNextAnchor()
                );
                response.setContent(content);
                
            } catch (RuntimeException e) {

                throw new RuntimeException("Không đọc được nội dung chapter từ EPUB", e);
            }
        }
        else {
            response.setContent("");
        }
        return response;

    }




    private Set<Long> getUnlockedChapterIds(Page<Chapter> chapters, boolean hasFullAccess) {
        if (hasFullAccess) {
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

        verifyOwnership(chapter.getBook());

        if (chapterRepository.existsByBookIdAndChapterNumberAndIdNot(chapter.getBook().getId(), request.getChapterNumber(), id)) {
            throw new AppException(ErrorCode.CHAPTER_NUMBER_EXISTED);
        }

        chapterMapper.updateChapter(chapter, request);

        if (Boolean.TRUE.equals(request.getIsFree())) {
            chapter.markAsFree();
        } else {
            BigDecimal price = request.getPrice();

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }
            chapter.setPrice(price);
            chapter.setIsFree(false);
        }

        return chapterMapper.toChapterResponse(chapterRepository.save(chapter));
    }



    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        verifyOwnership(chapter.getBook());

        chapterRepository.deleteById(id);
    }
}
