package server.mapper;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import server.model.EndpointHit;

import java.util.List;

public interface StatsMapper {

    EndpointHit toEntity(EndpointHitDto dto);

    ViewStatsDto toViewStatsDto(Object[] row);

    List<ViewStatsDto> toViewStatsDtoList(List<Object[]> rawResults);
}
