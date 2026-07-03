package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.chapter.ChapterCreationRequest;
import org.example.lv_backend.dto.request.chapter.ChapterUpdateRequest;
import org.example.lv_backend.dto.response.chapter.ChapterDetailResponse;
import org.example.lv_backend.dto.response.chapter.ChapterResponse;
import org.example.lv_backend.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

  

    @Mapping(target = "isLocked", source = "isLocked")
    ChapterResponse toChapterResponse(Chapter chapter, boolean isLocked);

    @Mapping(source = "chapter.book.id", target = "bookId")
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "isLocked", source = "isLocked")
    ChapterDetailResponse toChapterDetailResponse(Chapter chapter, boolean isLocked);


    @Mapping(target = "book", ignore = true)
    @Mapping(target = "isFree", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "sectionIndex", ignore = true)
    @Mapping(target = "fragmentId", ignore = true)
    @Mapping(target = "nextAnchor", ignore = true)
    void updateChapter(@MappingTarget Chapter chapter, ChapterUpdateRequest request);
}
