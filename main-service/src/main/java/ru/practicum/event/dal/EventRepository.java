package ru.practicum.event.dal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIdIn(List<Long> events);

    Page<Event> findByInitiator(User initiator, Pageable pageable);

    Optional<Event> findByIdAndInitiator(Long id, User initiator);

    Optional<Event> findByIdAndEventState(Long id, EventState eventState);

    boolean existsByCategory(Category category);

    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "LEFT JOIN FETCH e.location " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.eventState IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.id DESC")
    Page<Event> findByParameters(
            @Param("users") Collection<Long> users,
            @Param("states") Collection<EventState> states,
            @Param("categories") Collection<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Event e SET e.confirmedRequests = COALESCE(e.confirmedRequests, 0) + 1 " +
            "WHERE e.id = :eventId " +
            "AND (e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)")
    int incrementConfirmedRequestsIfWithinLimit(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE Event e SET e.confirmedRequests = GREATEST(COALESCE(e.confirmedRequests, 1) - 1, 0) " +
            "WHERE e.id = :eventId")
    void decrementConfirmedRequests(@Param("eventId") Long eventId);

    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "LEFT JOIN FETCH e.location " +
            "WHERE e.eventState = 'PUBLISHED' " +
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)")
    Page<Event> findByParametersForPublicController(
            @Param("text") String text,
            @Param("categories") Collection<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("paid") Boolean paid,
            @Param("onlyAvailable") boolean onlyAvailable,
            Pageable pageable);
}