package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto mapToCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category mapToCategory(NewCategoryDto request);

    List<CategoryDto> toCategoryDtoList(List<Category> categoryList);
}
