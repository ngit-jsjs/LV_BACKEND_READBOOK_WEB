package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByBookId(Long bookId, Pageable pageable);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);


    @Query("SELECT AVG(r.ratings) FROM Rating r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);
}
