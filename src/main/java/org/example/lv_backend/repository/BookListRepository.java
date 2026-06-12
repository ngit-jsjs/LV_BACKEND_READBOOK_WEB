package org.example.lv_backend.repository;

import org.example.lv_backend.entity.BookList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookListRepository extends JpaRepository<BookList,Long> {
    List<BookList> findByUser_Name(String username);
    boolean existsByNameAndUser_Name(String name, String username);
    boolean existsByNameAndIdNotAndUser_Name(String name, Long id, String username);
}
