package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChapterMapper {
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "chapterUnlocks", ignore = true)
    Chapter toChapter(ChapterCreationRequest request);

    @Mapping(source = "book.id", target = "bookId")
    ChapterResponse toChapterResponse(Chapter chapter);

    @Mapping(target = "book", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "chapterUnlocks", ignore = true)
    void updateChapter(@MappingTarget Chapter chapter, ChapterUpdateRequest request);
}
