package org.example.lv_backend.repository;

import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.entity.User;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    Page<Book> findByUserName(String name, Pageable pageable);

    Boolean existsByTitle (String title);

    Page<Book> findByStatusAndTitleContainingIgnoreCaseOrStatusAndAuthorContainingIgnoreCase(
            BookStatus status1, 
            String titleKeyword, 
            BookStatus status2, 
            String authorKeyword, 
            Pageable pageable
    );
    boolean existsByUserName(String author);

    boolean existsByTitleAndIdNot(String title, Long id);

    Page<Book> findByUserIdAndStatus(Long id, BookStatus status, Pageable pageable);
}
