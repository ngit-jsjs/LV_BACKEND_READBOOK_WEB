package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.chapter.ChapterUnlockResponse;
import org.example.lv_backend.entity.Chapter;
import org.example.lv_backend.entity.ChapterUnlock;
import org.example.lv_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChapterUnlockMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "chapter", source = "chapter")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    ChapterUnlock toChapterUnlock(User user, Chapter chapter);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "bookId", source = "chapter.book.id")
    @Mapping(target = "bookTitle", source = "chapter.book.title")
    @Mapping(target = "chapterId", source = "chapter.id")
    @Mapping(target = "chapterNumber", source = "chapter.chapterNumber")
    @Mapping(target = "chapterTitle", source = "chapter.title")
    @Mapping(target = "price", source = "chapter.price")
    ChapterUnlockResponse toChapterUnlockResponse(ChapterUnlock chapterUnlock);
}
