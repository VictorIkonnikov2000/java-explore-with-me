package ru.practicum.service;

import dto.RequestHitDto;
import dto.StatDto;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHit(RequestHitDto dto);

    List<StatDto> getStatDto(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}