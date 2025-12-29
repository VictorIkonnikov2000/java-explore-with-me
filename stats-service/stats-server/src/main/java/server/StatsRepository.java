package server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT h.app, h.uri, COUNT(CASE WHEN :unique = TRUE THEN DISTINCT h.ip ELSE h.ip END) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            ("AND h.uri IN :uris ") +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(CASE WHEN :unique = TRUE THEN DISTINCT h.ip ELSE h.ip END) DESC")
    List<Object[]> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris,
            @Param("unique") Boolean unique
    );
}
