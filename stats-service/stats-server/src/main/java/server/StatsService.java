package server;

import dto.EndpointHitDto;
import dto.ViewStatsDto;

import java.util.List;

public interface StatsService {

    /**
     * Сохраняет информацию о запросе.
     *
     * @param endpointHitDto DTO с информацией о запросе.
     */
    void saveHit(EndpointHitDto endpointHitDto);

    /**
     * Возвращает статистику по посещениям.
     *
     * @param start  Начало временного диапазона.
     * @param end    Конец временного диапазона.
     * @param uris   Список URI для фильтрации (необязательный параметр).
     * @param unique Определяет, считать ли только уникальные IP-адреса.
     * @return Список DTO со статистикой просмотров.
     */
    List<ViewStatsDto> getStats(String start, String end,
                                List<String> uris, Boolean unique);
}
