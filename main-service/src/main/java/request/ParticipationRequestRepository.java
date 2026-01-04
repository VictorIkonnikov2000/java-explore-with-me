package request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long userId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);  // Для поиска по списку ID
}
