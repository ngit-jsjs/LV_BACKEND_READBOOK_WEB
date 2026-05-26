package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    Page<Book> findByUserName(String name, Pageable pageable);

    Boolean existsByTitle (String title);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String keyword, String keyword1, Pageable pageable, List<BookStatus> statuses);

    boolean existsByUserName(String author);
}
