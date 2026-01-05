package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.repository.EventRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    public CategoryDto addCategory(NewCategoryDto request) {
        if (repository.existsByName(request.getName())) {
            throw new ConflictException("Category with name=" + request.getName() + " already exists");
        }

        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryDto(repository.save(category));
    }

    public void deleteCategory(Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (eventRepository.existsByCategory(category)) {
            throw new ConflictException("The category is not empty");
        }

        repository.deleteById(catId);
    }

    public CategoryDto updateCategory(Long catId, NewCategoryDto request) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        String newName = request.getName();
        if (!category.getName().equals(newName)) {
            if (repository.existsByName(newName)) {
                throw new ConflictException("Category name '" + newName + "' is already taken");
            }
            category.setName(newName);
        }

        return categoryMapper.toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        return repository.findById(catId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    @Transactional(readOnly = true)
    public Collection<CategoryDto> getAllCategories(int from, int size) {
        return categoryMapper.toCategoryDtoList(
                repository.findAll(PageRequest.of(from / size, size)).getContent()
        );
    }
}
