package category;



import category.dto.CategoryDto;
import category.dto.NewCategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    public Category toCategory(NewCategoryDto newCategoryDto) {
        if (newCategoryDto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }

    public void updateCategoryFromDto(CategoryDto categoryDto, Category category) {
        if (categoryDto == null) {
            return;
        }

        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
    }
}
