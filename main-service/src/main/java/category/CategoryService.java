package category;

import category.dto.CategoryDto;
import category.dto.NewCategoryDto;

import java.util.Collection;

public interface CategoryService {
    CategoryDto saveCategory(NewCategoryDto request);

    CategoryDto updateCategory(Long catId, NewCategoryDto request);

    void deleteCategory(Long catId);

    CategoryDto getCategoryById(Long catId);

    Collection<CategoryDto> getCategories(int from, int size);
}