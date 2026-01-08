package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT r FROM ParticipationRequest r WHERE r.requester.id = :userId")
    List<ParticipationRequest> findAllByRequesterId(@Param("userId") Long userId);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.id = :id AND r.requester.id = :userId")
    Optional<ParticipationRequest> findByIdAndRequesterId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.event.id = :eventId")
    List<ParticipationRequest> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = :status")
    List<ParticipationRequest> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.id IN :requestIds " +
            "AND r.event.id = :eventId " +
            "AND r.status = :status")
    List<ParticipationRequest> findValidRequestsForEvent(
            @Param("requestIds") Set<Long> requestIds,
            @Param("eventId") Long eventId,
            @Param("status") RequestStatus status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.requester.id = :userId AND r.status <> :status")
    boolean existsByEventIdAndRequesterIdAndStatusNot(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId,
            @Param("status") RequestStatus status);
}

