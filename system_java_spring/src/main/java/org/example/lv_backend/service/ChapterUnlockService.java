package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.chapter.ChapterUnlockResponse;
import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.entity.ChapterUnlock;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.ChapterUnlockMapper;
import org.example.lv_backend.repository.ChapterRepository;
import org.example.lv_backend.repository.ChapterUnlockRepository;
import org.example.lv_backend.repository.UserRepository;
import org.example.lv_backend.util.SecurityUtil;
import org.example.lv_backend.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChapterUnlockService {

    private final ChapterUnlockRepository chapterUnlockRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final ChapterUnlockMapper chapterUnlockMapper;
    private final SecurityUtil securityUtil;

    @Transactional
    public void unlockChapter(Long chapterId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUsername = auth.getName();
        User user = userRepository.findByName(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_EXISTED));

        if (!securityUtil.isAdmin() && chapter.getBook().getStatus() == BookStatus.UNAVAILABLE) {
            throw new AppException(ErrorCode.CHAPTER_NOT_EXISTED);
        }

        if (Boolean.TRUE.equals(chapter.getIsFree())) {
            throw new AppException(ErrorCode.CHAPTER_ALREADY_FREE);
        }

        if (chapterUnlockRepository.checkIfUnlocked(user.getId(), chapter.getId())) {
            throw new AppException(ErrorCode.CHAPTER_ALREADY_UNLOCKED);
        }

        if (user.getAmount().compareTo(chapter.getPrice()) < 0) {
            throw new AppException(ErrorCode.NOT_ENOUGH_COIN);
        }

        user.setAmount(user.getAmount().subtract(chapter.getPrice()));
        userRepository.save(user);


        ChapterUnlock chapterUnlock = chapterUnlockMapper.toChapterUnlock(user, chapter);
        chapterUnlockRepository.save(chapterUnlock);
    }

    @Transactional(readOnly = true)
    public Page<ChapterUnlockResponse> getMyUnlockHistory(int page, int size) {
        String currentUsername = securityUtil.getCurrentUsername();
        User user = userRepository.findByName(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page, size);
        return chapterUnlockRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(chapterUnlockMapper::toChapterUnlockResponse);
    }

    @Transactional(readOnly = true)
    public Page<ChapterUnlockResponse> getUnlocksByUserAdmin(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chapterUnlockRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(chapterUnlockMapper::toChapterUnlockResponse);
    }
}
