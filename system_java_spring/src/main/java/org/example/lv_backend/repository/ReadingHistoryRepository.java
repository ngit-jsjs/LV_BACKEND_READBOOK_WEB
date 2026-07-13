package org.example.lv_backend.repository;

import org.example.lv_backend.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    Optional<ReadingHistory> findByUser_IdAndBook_Id(Long userId, Long bookId);

    Page<ReadingHistory> findByUser_Id(Long userId, Pageable pageable);
}
