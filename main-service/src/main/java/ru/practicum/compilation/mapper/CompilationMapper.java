package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final EventMapper eventMapper;

    /**
     * Преобразование сущности в DTO для ответа
     */
    public CompilationDto toCompilationDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                // Используем eventMapper для конвертации Set<Event> в List<EventShortDto>
                .events(eventMapper.toShortDtoList(List.copyOf(compilation.getEvents())))
                .build();
    }

    /**
     * Создание новой сущности из NewCompilationDto
     * events передаются отдельно, так как в DTO только их ID
     */
    public Compilation toCompilation(NewCompilationDto request) {
        if (request == null) {
            return null;
        }

        return Compilation.builder()
                .title(request.getTitle())
                .pinned(request.getPinned() != null ? request.getPinned() : false)
                .events(new HashSet<>()) // Будет заполнено в сервисе
                .build();
    }

    /**
     * Частичное обновление сущности
     */
    public void updateFromRequest(UpdateCompilationRequest request, Compilation compilation) {
        if (request == null || compilation == null) {
            return;
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            compilation.setTitle(request.getTitle());
        }
    }

    /**
     * Преобразование списка сущностей в список DTO
     */
    public List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList) {
        if (compilationList == null) {
            return List.of();
        }
        return compilationList.stream()
                .map(this::toCompilationDto)
                .collect(Collectors.toList());
    }
}

