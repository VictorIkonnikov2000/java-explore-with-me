package ru.practicum.explorewithme.client;

import ru.practicum.explorewithme.dto.EndpointHit;
import ru.practicum.explorewithme.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {

    void hit(EndpointHit endpointHit);

    List<ViewStats> getStats(String start, String end,
                             List<String> uris, Boolean unique);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                             List<String> uris, Boolean unique);
}