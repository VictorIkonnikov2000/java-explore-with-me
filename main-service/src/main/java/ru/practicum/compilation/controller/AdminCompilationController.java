package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid NewCompilationDto request) {
        log.info("Admin: Получен запрос на создание подборки: {}", request.getTitle());
        return compilationService.addCompilation(request);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable @Positive Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest request) {
        log.info("Admin: Получен запрос на обновление подборки id={}", compId);
        return compilationService.updateCompilation(compId, request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        log.info("Admin: Получен запрос на удаление подборки id={}", compId);
        compilationService.deleteCompilation(compId);
    }
}
