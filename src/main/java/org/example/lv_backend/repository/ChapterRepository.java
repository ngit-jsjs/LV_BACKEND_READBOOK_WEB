package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Page<Chapter> findByBookIdOrderByChapterNumberAsc(Long bookId, Pageable pageable);
    boolean existsByBookIdAndChapterNumberAndIdNot(Long bookId, Integer chapterNumber, Long id);
}
