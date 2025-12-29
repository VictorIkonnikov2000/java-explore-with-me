import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void saveHit(String app, String uri, String ip, LocalDateTime timestamp);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}

