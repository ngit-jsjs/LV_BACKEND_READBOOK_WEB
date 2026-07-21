package org.example.lv_backend.repository;

import org.example.lv_backend.entity.PopularBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularBookRepository extends JpaRepository<PopularBook, Long> {
    
    @Query("SELECT pb FROM PopularBook pb JOIN FETCH pb.book b")
    List<PopularBook> findAllWithBook();
}
