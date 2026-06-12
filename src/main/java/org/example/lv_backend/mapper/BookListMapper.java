package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.booklist.BookListRequest;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.entity.BookList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookListMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    BookList toBookList(BookListRequest request);

    @Mapping(target = "bookIds", ignore = true)
    BookListResponse toBookListResponse(BookList bookList);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateBookList(@MappingTarget BookList bookList, BookListRequest request);
}
