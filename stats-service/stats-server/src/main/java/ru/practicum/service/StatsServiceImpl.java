package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.EndpointHitRepository;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    // Внедряем маппер как компонент
    private final EndpointHitMapper endpointHitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        // Используем экземпляр бина endpointHitMapper
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(dto);
        endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        // Логика выбора метода репозитория в зависимости от уникальности IP
        if (unique) {
            return endpointHitRepository.findUniqueStatsAll(start, end, uris);
        } else {
            return endpointHitRepository.findNotUniqueStats(start, end, uris);
        }
    }
}
