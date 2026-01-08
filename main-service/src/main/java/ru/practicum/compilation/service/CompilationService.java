package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    public CompilationDto addCompilation(NewCompilationDto request) {
        if (request == null) {
            log.error("Попытка создания подборки с null телом запроса");
            throw new BadRequestException("Payload is null. Cannot create compilation.");
        }

        log.info("Добавление новой подборки: {}", request.getTitle());
        Compilation compilation = compilationMapper.toCompilation(request);

        if (request.getPinned() == null) {
            compilation.setPinned(false);
        }

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<Event> eventList = eventRepository.findByIdIn(request.getEvents());
            log.debug("Для подборки найдено {} событий", eventList.size());
            compilation.setEvents(new HashSet<>(eventList));
        }

        Compilation savedCompilation = repository.save(compilation);
        log.info("Подборка была создана с id={}", savedCompilation.getId());
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки id={}", compId);
        if (!repository.existsById(compId)) {
            log.warn("Ошибка удаления: подборка id={} не найдена", compId);
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId));
        }
        repository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        if (request == null) {
            log.error("Попытка обновления подборки id={} с null телом запроса", compId);
            throw new BadRequestException("Payload is null. Cannot update compilation.");
        }

        log.info("Обновление подборки id={}", compId);
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Ошибка обновления: подборка id={} не найдена", compId);
                    return new NotFoundException(String.format("Compilation with id=%d was not found", compId));
                });

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getEvents() != null) {
            if (request.getEvents().isEmpty()) {
                log.debug("Очистка списка событий в подборке id={}", compId);
                compilation.getEvents().clear();
            } else {
                List<Event> eventList = eventRepository.findByIdIn(request.getEvents());
                log.debug("Обновление событий: найдено {} событий для подборки", eventList.size());
                compilation.setEvents(new HashSet<>(eventList));
            }
        }

        return compilationMapper.toCompilationDto(repository.save(compilation));
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.info("Запрошена подборка id={}", compId);
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Подборка id={} не найдена", compId);
                    return new NotFoundException(String.format("Compilation with id=%d was not found", compId));
                });
        return compilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    public Collection<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        log.info("Запрошен список подборок: pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned == null) {
            compilations = repository.findAll(pageable).getContent();
        } else {
            compilations = repository.findByPinned(pinned, pageable).getContent();
        }

        return compilationMapper.toCompilationDtoList(compilations);
    }
}
