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



    Boolean existsByTitle (String title);

    @Query("SELECT b FROM Book b WHERE b.status = :status AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> findByStatusAndKeyword(@Param("status") BookStatus status, @Param("keyword") String keyword, Pageable pageable);

    boolean existsByTitleAndIdNot(String title, Long id);


    Page<Book> findByStatus(BookStatus bookStatus, Pageable pageable);
    Page<Book> findByBookLists_Id(Long bookListId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.user.name = :name AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> findByKeyword(@Param("name") String name, @Param("keyword") String keyword, Pageable pageable);
}
