package ru.practicum.explorewithme.client;

import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {

    void hit(EndpointHitDto endpointHitDto);

    /**
     * Статистика по посещениям
     * @param start дата и время начала диапазона
     * @param end дата и время конца диапазона
     * @param uris список URI для фильтрации
     * @param unique учитывать только уникальные посещения
     * @return список статистики
     */
    List<ViewStatsDto> getStats(String start, String end,
                                List<String> uris, Boolean unique);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                List<String> uris, Boolean unique);
}