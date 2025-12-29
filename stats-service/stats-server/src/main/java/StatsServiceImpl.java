import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto hit = new EndpointHitDto(null, app, uri, ip, timestamp);
        statsRepository.save(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<Object[]> results;
        if (unique) {
            results = statsRepository.findUniqueStats(start, end, uris);
        } else {
            results = statsRepository.findStats(start, end, uris);
        }

        return results.stream()
                .map(result -> new ViewStatsDto((String) result[0], (String) result[1], ((Number) result[2]).longValue()))
                .collect(Collectors.toList());
    }
}

