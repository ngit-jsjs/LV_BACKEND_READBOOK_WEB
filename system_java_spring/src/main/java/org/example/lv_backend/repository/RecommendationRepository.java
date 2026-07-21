package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    @Query("SELECT r FROM Recommendation r JOIN FETCH r.book b WHERE r.user.id = :userId ORDER BY r.score DESC")
    List<Recommendation> findByUserIdOrderByScoreDesc(@Param("userId") Long userId);
    
}
