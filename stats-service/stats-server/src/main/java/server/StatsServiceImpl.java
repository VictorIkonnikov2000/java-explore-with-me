package server;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import dto.EndpointHitDto;
import dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit entity = EndpointHitMapper.toEntity(endpointHitDto);
        statsRepository.save(entity);
        log.debug("Запрос сохранён: app={}, uri={}", entity.getApp(), entity.getUri());
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        return mapToViewStats(statsRepository.findStats(start, end, uris, unique));
    }

    private List<ViewStatsDto> mapToViewStats(List<Object[]> rawResults) {
        List<ViewStatsDto> viewStatsDtoList = new java.util.ArrayList<>();
        for (Object[] row : rawResults) {
            viewStatsDtoList.add(new ViewStatsDto(
                    (String) row[0],
                    (String) row[1],
                    (Long) row[2]
            ));
        }
        return viewStatsDtoList;
    }
}

