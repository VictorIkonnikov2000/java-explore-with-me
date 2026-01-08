package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    // Преобразование из Entity в CategoryDto
    public CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Преобразование из NewCategoryDto в Entity
    public Category toCategory(NewCategoryDto request) {
        if (request == null) {
            return null;
        }
        return Category.builder()
                .name(request.getName())
                .build();
    }

    // Преобразование списка
    public List<CategoryDto> toCategoryDtoList(List<Category> categoryList) {
        if (categoryList == null) {
            return null;
        }
        return categoryList.stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }
}
