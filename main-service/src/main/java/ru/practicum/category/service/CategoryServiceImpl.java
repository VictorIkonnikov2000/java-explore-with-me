package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.dal.EventRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto saveCategory(NewCategoryDto request) {

        if (request == null) {
            throw new BadRequestException("Запрос на добавление новой категории не может быть null");
        }

        isContainsCategoryByName(request.getName());
        Category category = categoryMapper.mapToCategory(request);
        Category categorySave = repository.save(category);
        return categoryMapper.mapToCategoryDto(categorySave);
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = isContainsCategory(catId);

        if (eventRepository.existsByCategory(category)) {
            throw new ConflictException("К данной категории привязано событие");
        }

        repository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto request) {

        if (request == null) {
            throw new BadRequestException("Запрос на изменение категории не может быть null");
        }

        Category category = isContainsCategory(catId);

        if (category.getName().equals(request.getName())) {
            return categoryMapper.mapToCategoryDto(category);
        }

        isContainsCategoryByName(request.getName());
        category.setName(request.getName());
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        Category category = isContainsCategory(catId);
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Category> categoryPage = repository.findAll(pageable);
        List<Category> categoryList = categoryPage.getContent();
        return categoryMapper.toCategoryDtoList(categoryList);
    }

    private Category isContainsCategory(Long id) {
        Optional<Category> optCategories = repository.findById(id);

        if (optCategories.isEmpty()) {
            throw new NotFoundException("Категория с id: " + id + " в базе отсутствует");
        }

        return optCategories.get();
    }

    private void isContainsCategoryByName(String name) {

        if (repository.existsByName(name)) {
            throw new ConflictException("Категория с именем " + name + " в базе данных существует");
        }

    }
}
