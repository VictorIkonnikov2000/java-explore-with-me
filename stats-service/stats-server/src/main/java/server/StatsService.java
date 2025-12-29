package server;

import dto.EndpointHitDto;
import dto.ViewStatsDto;

import java.util.List;

public interface StatsService {
    void saveHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end,
                                List<String> uris, Boolean unique);
}
