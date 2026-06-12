package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.category.CategoryCreationRequest;
import org.example.lv_backend.dto.response.category.CategoryResponse;
import org.example.lv_backend.entity.Category;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.CategoryMapper;
import org.example.lv_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse createCategory(CategoryCreationRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryCreationRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        categoryMapper.updateCategory(category, request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        if (category.getBooks() != null) {
            for (Book book : category.getBooks()) {
                book.getCategories().remove(category);
            }
        }
        categoryRepository.delete(category);
    }
}
