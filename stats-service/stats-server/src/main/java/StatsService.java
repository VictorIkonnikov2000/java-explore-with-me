import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(EndpointHitDto hit);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}