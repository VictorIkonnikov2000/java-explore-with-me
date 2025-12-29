package client;

import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.util.List;

public interface StatsClient {

    void hit(EndpointHitDto endpointHit);

    List<ViewStatsDto> getStats(
            String start,
            String end,
            List<String> uris,
            Boolean unique
    );
}