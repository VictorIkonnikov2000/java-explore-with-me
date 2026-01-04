package category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import category.dto.CategoryDto;
import category.dto.NewCategoryDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto mapToCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category mapToCategory(NewCategoryDto request);

    List<CategoryDto> toCategoryDtoList(List<Category> categoryList);
}
