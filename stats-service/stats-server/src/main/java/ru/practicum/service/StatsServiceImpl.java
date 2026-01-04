package ru.practicum.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.EndpointHitRepository;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.error.exceptions.ValidationException; // Используйте ваше исключение для 400 ошибки

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        log.info("Сохранение hit для uri: {}", dto.getUri());
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(dto);
        endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        // Ручная проверка для прохождения теста на статус 400
        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Ошибка валидации: start {} позже end {}", start, end);
            // Если выбросить это исключение, ErrorHandler вернет 400
            throw new ValidationException("Start time must be before end time");
        }

        // Логика выбора метода репозитория (аналогично вашему прошлому коду,
        // но теперь репозиторий сразу возвращает ViewStatsDto)
        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return endpointHitRepository.findUniqueStatsAll(start, end);
            } else {
                return endpointHitRepository.findUniqueStatsByUris(start, end, uris);
            }
        } else {
            if (uris == null || uris.isEmpty()) {
                return endpointHitRepository.findStatsAll(start, end);
            } else {
                return endpointHitRepository.findStatsByUris(start, end, uris);
            }
        }
    }
}

