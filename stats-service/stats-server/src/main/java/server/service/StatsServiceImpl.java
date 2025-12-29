package server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import dto.EndpointHitDto;
import dto.ViewStatsDto;
import server.mapper.StatsMapper;
import server.model.EndpointHit;
import server.repository.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.info("Получен запрос на сохранение хита: app={}, uri={}, ip={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(), endpointHitDto.getIp());

        validateEndpointHit(endpointHitDto);

        EndpointHit entity = statsMapper.toEntity(endpointHitDto);
        statsRepository.save(entity);

        log.info("Хит успешно сохранен с ID: {}", entity.getId());
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end,
                                       List<String> uris, Boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        String decodedStart = decodeDateTime(start);
        String decodedEnd = decodeDateTime(end);

        LocalDateTime startTime = parseDateTime(decodedStart);
        LocalDateTime endTime = parseDateTime(decodedEnd);

        validateTimeRange(startTime, endTime);

        List<Object[]> rawResults;
        if (Boolean.TRUE.equals(unique)) {
            log.debug("Выборка уникальной статистики");
            rawResults = getUniqueStats(startTime, endTime, uris);
        } else {
            log.debug("Выборка общей статистики");
            rawResults = getAllStats(startTime, endTime, uris);
        }

        List<ViewStatsDto> results = statsMapper.toViewStatsDtoList(rawResults);
        log.info("Статистика успешно получена, количество записей: {}", results.size());

        return results;
    }

    private List<Object[]> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return statsRepository.findUniqueStatsAll(start, end);
        } else {
            return statsRepository.findUniqueStatsByUris(start, end, uris);
        }
    }

    private List<Object[]> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return statsRepository.findStatsAll(start, end);
        } else {
            return statsRepository.findStatsByUris(start, end, uris);
        }
    }

    private void validateEndpointHit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto.getApp() == null || endpointHitDto.getApp().trim().isEmpty()) {
            log.warn("Валидация провалена: пустое название приложения");
            throw new IllegalArgumentException("Название приложения не может быть пустым");
        }
        if (endpointHitDto.getUri() == null || endpointHitDto.getUri().isEmpty()) {
            log.warn("Валидация провалена: пустой URI");
            throw new IllegalArgumentException("URI не может быть пустым");
        }
        if (endpointHitDto.getIp() == null || endpointHitDto.getIp().isEmpty()) {
            log.warn("Валидация провалена: пустой IP");
            throw new IllegalArgumentException("IP не может быть пустым");
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.warn("Ошибка валидации времени: start {} после end {}", start, end);
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }
    }

    private String decodeDateTime(String dateTime) {
        try {
            return URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Ошибка декодирования даты: {}", dateTime);
            return dateTime;
        }
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("Ошибка парсинга даты: {}. Ожидаемый формат: yyyy-MM-dd HH:mm:ss", dateTime);
            throw new IllegalArgumentException(
                    "Некорректный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss. Получено: " + dateTime, e);
        }
    }
}


