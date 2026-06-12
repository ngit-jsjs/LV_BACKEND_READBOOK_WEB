package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.category.CategoryCreationRequest;
import org.example.lv_backend.dto.response.category.CategoryResponse;
import org.example.lv_backend.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "books", ignore = true)
    Category toCategory(CategoryCreationRequest request);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "books", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryCreationRequest request);
}
