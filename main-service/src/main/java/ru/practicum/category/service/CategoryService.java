package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    public CategoryDto addCategory(NewCategoryDto request) {
        log.info("Добавление новой категории: {}", request.getName());
        if (repository.existsByName(request.getName())) {
            log.warn("Конфликт: категория с именем '{}' уже существует", request.getName());
            throw new ConflictException("Category with name=" + request.getName() + " already exists");
        }

        Category category = categoryMapper.toCategory(request);
        CategoryDto saved = categoryMapper.toCategoryDto(repository.save(category));
        log.info("Категория успешно сохранена с id={}", saved.getId());
        return saved;
    }

    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id={}", catId);
        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.warn("Ошибка удаления: категория с id={} не найдена", catId);
                    return new NotFoundException("Category with id=" + catId + " was not found");
                });

        if (eventRepository.existsByCategory(category)) {
            log.warn("Конфликт удаления: категория с id={} используется в событиях", catId);
            throw new ConflictException("The category is not empty");
        }

        repository.deleteById(catId);
        log.info("Категория с id={} удалена", catId);
    }

    public CategoryDto updateCategory(Long catId, NewCategoryDto request) {
        log.info("Обновление категории id={}", catId);
        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.warn("Ошибка обновления: категория с id={} не найдена", catId);
                    return new NotFoundException("Category with id=" + catId + " was not found");
                });

        String newName = request.getName();
        if (!category.getName().equals(newName)) {
            if (repository.existsByName(newName)) {
                log.warn("Конфликт обновления: имя '{}' уже занято", newName);
                throw new ConflictException("Category name '" + newName + "' is already taken");
            }
            log.info("Имя категории id={} изменено с '{}' на '{}'", catId, category.getName(), newName);
            category.setName(newName);
        }

        return categoryMapper.toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        log.info("Получение категории по id={}", catId);
        return repository.findById(catId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> {
                    log.warn("Категория id={} не найдена", catId);
                    return new NotFoundException("Category with id=" + catId + " was not found");
                });
    }

    @Transactional(readOnly = true)
    public Collection<CategoryDto> getAllCategories(int from, int size) {
        log.info("Получение списка категорий: from={}, size={}", from, size);
        return categoryMapper.toCategoryDtoList(
                repository.findAll(PageRequest.of(from / size, size)).getContent()
        );
    }
}

