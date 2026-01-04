package event;



import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId);  // Для получения событий пользователя
    // Добавьте методы для поиска по критериям (админский контроллер)
}
