package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.dal.EndpointHitRepository;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.EndpointHit;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitMapper hitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void createHit(RequestHitDto dto) {
        EndpointHit endpointHit = hitMapper.mapToEndpointHit(dto);
        EndpointHit saveHit = endpointHitRepository.save(endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatDto> getStatDto(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start == null || end == null) {
            throw new BadRequestException("Временной промежуток должен быть задан");
        }

        if (end.isBefore(start)) {
            throw new BadRequestException("Конец диапазона не может начинаться раньше по времени, чем начало диапазона");
        }

        if (unique) {
            return endpointHitRepository.findUniqueStats(start, end, uris);
        } else {
            return endpointHitRepository.findNotUniqueStats(start, end, uris);
        }
    }
}
