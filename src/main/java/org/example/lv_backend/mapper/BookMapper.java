package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.book.BookListSummaryResponse;
import org.example.lv_backend.dto.response.book.BookSummaryResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookList;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookSummaryResponse toBookSummary(Book book);

    List<BookSummaryResponse> toBookSummaryList(List<Book> books);

    List<BookSummaryResponse> toBookSummaryListFromSet(Set<Book> books);

    BookListSummaryResponse toBookListSummary(BookList bookList);

    List<BookListSummaryResponse> toBookListSummaryList(List<BookList> bookLists);
}

