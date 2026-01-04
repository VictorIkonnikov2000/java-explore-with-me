package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.StatDto;
import ru.practicum.model.EndpointHit;
import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query(
            """
            SELECT new ru.practicum.dto.StatDto(eh.app, eh.uri, COUNT(eh.id))
            FROM EndpointHit eh
            WHERE eh.timestamp
            BETWEEN :start
            AND :end
            AND (:uris IS NULL OR eh.uri in :uris)
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(eh.id) DESC
            """
    )
    List<StatDto> findNotUniqueStats(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query(
            """
            SELECT new ru.practicum.dto.StatDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
            FROM EndpointHit eh
            WHERE eh.timestamp
            BETWEEN :start
            AND :end
            AND (:uris IS NULL OR eh.uri in :uris)
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(DISTINCT eh.ip) DESC
            """
    )
    List<StatDto> findUniqueStats(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("uris") List<String> uris);
}