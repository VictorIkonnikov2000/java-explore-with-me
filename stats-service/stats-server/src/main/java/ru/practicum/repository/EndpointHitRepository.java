package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query(
            """
            SELECT new ru.practicum.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.id))
            FROM EndpointHit eh
            WHERE eh.timestamp BETWEEN :start AND :end
            AND eh.uri IN (:uris)
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(eh.id) DESC
            """
    )
    List<ViewStatsDto> findNotUniqueStatsByUris(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("uris") List<String> uris);

    @Query(
            """
            SELECT new ru.practicum.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.id))
            FROM EndpointHit eh
            WHERE eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(eh.id) DESC
            """
    )
    List<ViewStatsDto> findNotUniqueStatsAll(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query(
            """
            SELECT new ru.practicum.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
            FROM EndpointHit eh
            WHERE eh.timestamp BETWEEN :start AND :end
            AND eh.uri IN (:uris)
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(DISTINCT eh.ip) DESC
            """
    )
    List<ViewStatsDto> findUniqueStatsByUris(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);

    @Query(
            """
            SELECT new ru.practicum.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
            FROM EndpointHit eh
            WHERE eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(DISTINCT eh.ip) DESC
            """
    )
    List<ViewStatsDto> findUniqueStatsAll(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}
