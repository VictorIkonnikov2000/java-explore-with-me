package client;

import dto.EndpointHitDto;
import dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {

    void hit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end,
                                List<String> uris, Boolean unique);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                List<String> uris, Boolean unique);
}