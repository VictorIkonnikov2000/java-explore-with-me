package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventAndRequesterAndStatusNot(Event event, User requester, RequestStatus status);

    Collection<ParticipationRequest> findByRequester(User requester);

    Optional<ParticipationRequest> findByIdAndRequester(Long id, User requester);

    Collection<ParticipationRequest> findByEvent(Event event);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.id IN :requestIds " +
            "AND r.event.id = :eventId " +
            "AND r.status = :status")
    List<ParticipationRequest> findValidRequestsForEvent(
            @Param("requestIds") Set<Long> requestIds,
            @Param("eventId") Long eventId,
            @Param("status") RequestStatus status);
}