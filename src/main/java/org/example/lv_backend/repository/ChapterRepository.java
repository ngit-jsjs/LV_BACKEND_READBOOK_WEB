package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByBookIdOrderByChapterNumberAsc(Long bookId);
    boolean existsByBookIdAndChapterNumber(Long bookId, Long chapterNumber);
    boolean existsByBookIdAndChapterNumberAndIdNot(Long bookId, Long chapterNumber, Long id);
}
