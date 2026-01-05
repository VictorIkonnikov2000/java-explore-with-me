package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    public CompilationDto addCompilation(NewCompilationDto request) {
        if (request == null) {
            throw new BadRequestException("Payload is null. Cannot create compilation.");
        }

        // Используем маппер (метод toCompilation)
        Compilation compilation = compilationMapper.toCompilation(request);

        if (request.getPinned() == null) {
            compilation.setPinned(false);
        }

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<Event> eventList = eventRepository.findByIdIn(request.getEvents());
            compilation.setEvents(new HashSet<>(eventList));
        }

        Compilation savedCompilation = repository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    public void deleteCompilation(Long compId) {
        if (!repository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId));
        }
        repository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        if (request == null) {
            throw new BadRequestException("Payload is null. Cannot update compilation.");
        }

        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getEvents() != null) {
            if (request.getEvents().isEmpty()) {
                compilation.getEvents().clear();
            } else {
                List<Event> eventList = eventRepository.findByIdIn(request.getEvents());
                compilation.setEvents(new HashSet<>(eventList));
            }
        }

        return compilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));
        return compilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    public Collection<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
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
