package server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    // Общая функция для запросов статистики (обычной и уникальной)
    @Query("SELECT h.app, h.uri, COUNT(:distinctClause h.ip) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(:distinctClause h.ip) DESC")
    List<Object[]> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris,
            @Param("distinctClause") String distinctClause  // "DISTINCT" или ""
    );


    default List<Object[]> findStatsAll(LocalDateTime start, LocalDateTime end) {
        return findStats(start, end, null, "");  // Без uris и без DISTINCT
    }

    default List<Object[]> findStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return findStats(start, end, uris, "");   // С uris, но без DISTINCT
    }

    default List<Object[]> findUniqueStatsAll(LocalDateTime start, LocalDateTime end) {
        return findStats(start, end, null, "DISTINCT");  // Без uris, но с DISTINCT
    }

    default List<Object[]> findUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return findStats(start, end, uris, "DISTINCT");   // С uris и с DISTINCT
    }
}
