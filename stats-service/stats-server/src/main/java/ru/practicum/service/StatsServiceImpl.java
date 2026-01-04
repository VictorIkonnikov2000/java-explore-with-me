package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.EndpointHitRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EndpointHitMapper endpointHitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(dto);
        EndpointHit saveHit = endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start == null || end == null) {
            throw new BadRequestException("Временной промежуток должен быть задан");
        }

        if (end.isBefore(start)) {
            throw new BadRequestException("Конец диапазона не может начинаться раньше по времени, чем начало диапазона");
        }

        if (unique) {
            return endpointHitRepository.findUniqueStatsAll(start, end, uris);
        } else {
            return endpointHitRepository.findNotUniqueStats(start, end, uris);
        }
    }
}
