package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.entity.ChapterUnlock;
import org.example.lv_backend.entity.ChapterUnlockId;
import org.example.lv_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChapterUnlockRepository extends JpaRepository<ChapterUnlock, ChapterUnlockId> {
    @Query(value = "SELECT COUNT(1) > 0 FROM chapter_unlocks WHERE user_id = :userId AND chapter_id = :chapterId", nativeQuery = true)
    boolean checkIfUnlocked(@Param("userId") Long userId, @Param("chapterId") Long chapterId);

    @Query(value = "SELECT chapter_id FROM chapter_unlocks WHERE user_id = :userId AND chapter_id IN :chapterIds", nativeQuery = true)
    Set<Long> findUnlockedChapterIds(@Param("userId") Long userId, @Param("chapterIds") List<Long> chapterIds);

    @Query(value = "SELECT COUNT(1) > 0 FROM chapter_unlocks cu JOIN chapters c ON cu.chapter_id = c.id WHERE c.book_id = :bookId", nativeQuery = true)
    boolean existsByChapter_Book_Id(@Param("bookId") Long bookId);
}
