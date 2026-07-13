package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.author.AuthorCreationRequest;
import org.example.lv_backend.dto.response.author.AuthorResponse;
import org.example.lv_backend.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(target = "books", ignore = true)
    Author toAuthor(AuthorCreationRequest request);

    AuthorResponse toAuthorResponse(Author author);

    @Mapping(target = "books", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateAuthor(@MappingTarget Author author, AuthorCreationRequest request);
}
