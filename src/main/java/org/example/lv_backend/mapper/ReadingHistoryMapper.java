package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.readinghistory.ReadingHistoryResponse;
import org.example.lv_backend.entity.ReadingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ReadingHistoryMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "lastChapterId", ignore = true)
    @Mapping(target = "lastChapterNumber", ignore = true)
    @Mapping(target = "lastChapterTitle", ignore = true)
    @Mapping(target = "bookTitle", ignore = true)
    @Mapping(target = "bookAuthor", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    ReadingHistoryResponse toReadingHistoryResponse(ReadingHistory readingHistory);
}
