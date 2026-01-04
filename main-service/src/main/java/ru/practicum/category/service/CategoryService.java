package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.Collection;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto request);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, NewCategoryDto request);

    Collection<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);
}