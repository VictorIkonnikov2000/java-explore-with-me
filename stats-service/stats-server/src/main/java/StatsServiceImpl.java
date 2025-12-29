import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository hitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto hit) {
        hitRepository.save(EndpointHitMapper.toEntity(hit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<String> filterUris = (uris == null || uris.isEmpty()) ? null : uris;
        if (unique) {
            return hitRepository.findStatsUnique(start, end, filterUris);
        }
        return hitRepository.findStats(start, end, filterUris);
    }
}