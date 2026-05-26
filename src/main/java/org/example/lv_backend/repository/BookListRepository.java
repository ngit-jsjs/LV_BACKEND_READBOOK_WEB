package org.example.lv_backend.repository;

import org.example.lv_backend.entity.BookList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookListRepository extends JpaRepository<BookList,Long> {

}
