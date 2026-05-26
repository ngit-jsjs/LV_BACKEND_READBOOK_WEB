package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.book.BookCreationRequest;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookList;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {


    BookResponse toBookResponse(Book book);

    Book toBook(BookCreationRequest request);

    void updateBook(Book book, BookCreationRequest request);
}

