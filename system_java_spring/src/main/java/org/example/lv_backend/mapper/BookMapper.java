package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.book.BookCreationRequest;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookList;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.lv_backend.entity.Category;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(source = "user.name", target = "uploaderName")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "publisherId", source = "publisher.id")
    @Mapping(target = "author", source = "author.name")
    @Mapping(target = "publisher", source = "publisher.name")
    BookResponse toBookResponse(Book book);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    Book toBook(BookCreationRequest request);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    void updateBook(@MappingTarget Book book, BookCreationRequest request);



    default Set<String> mapCategories(Set<Category> categories) {
        if (categories == null) {
            return new java.util.HashSet<>();
        }
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }
}
