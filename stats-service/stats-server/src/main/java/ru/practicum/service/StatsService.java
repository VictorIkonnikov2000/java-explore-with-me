package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatDto;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHit(EndpointHitDto dto);

    List<StatDto> getStatDto(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}