package org.example.lv_backend.repository;

import org.example.lv_backend.entity.BookList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookListRepository extends JpaRepository<BookList,Long> {
    List<BookList> findByUser_Id(Long userId);
    Page<BookList> findByUser_Id(Long userId, Pageable pageable);
    boolean existsByNameAndUser_Id(String name, Long userId);
    boolean existsByNameAndIdNotAndUser_Id(String name, Long id, Long userId);
}
