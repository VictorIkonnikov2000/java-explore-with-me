package server.mapper;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import org.springframework.stereotype.Component;
import server.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatsMapperImpl implements StatsMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp().trim())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(parseDateTime(dto.getTimestamp()))
                .build();
    }

    @Override
    public ViewStatsDto toViewStatsDto(Object[] row) {
        return new ViewStatsDto(
                (String) row[0],
                (String) row[1],
                (Long) row[2]
        );
    }

    @Override
    public List<ViewStatsDto> toViewStatsDtoList(List<Object[]> rawResults) {

        List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
        for (Object[] row : rawResults) {
            viewStatsDtoList.add(toViewStatsDto(row));
        }
        return viewStatsDtoList;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некорректный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss. Получено: " + dateTime, e);
        }
    }
}
