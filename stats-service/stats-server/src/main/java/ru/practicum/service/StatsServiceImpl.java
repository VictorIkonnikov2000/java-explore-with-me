package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EndpointHitMapper endpointHitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        log.info("Сохранение хита для приложения: {}, uri: {}", dto.getApp(), dto.getUri());

        // Маппим и сохраняем
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(dto);
        endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        validateTimeRange(start, end);

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return endpointHitRepository.findUniqueStatsAll(start, end);
            } else {
                return endpointHitRepository.findUniqueStatsByUris(start, end, uris);
            }
        } else {
            if (uris == null || uris.isEmpty()) {
                return endpointHitRepository.findNotUniqueStatsAll(start, end);
            } else {
                return endpointHitRepository.findNotUniqueStatsByUris(start, end, uris);
            }
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Временной промежуток должен быть задан полностью (start и end)");
        }
        if (start.isAfter(end)) {
            log.warn("Ошибка валидации: дата начала {} позже даты конца {}", start, end);
            throw new BadRequestException("Дата начала не может быть позже даты окончания");
        }
    }
}

