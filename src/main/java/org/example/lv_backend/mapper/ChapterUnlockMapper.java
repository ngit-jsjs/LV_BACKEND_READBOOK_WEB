package org.example.lv_backend.mapper;

import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.entity.ChapterUnlock;
import org.example.lv_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface ChapterUnlockMapper {


    @Mapping(target = "user", source = "user")
    @Mapping(target = "chapter", source = "chapter")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    ChapterUnlock toChapterUnlock(User user, Chapter chapter);
}
