package client;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;

    public void hit(EndpointHitDto endpointHitDto) {
        log.info("Отправка данных о просмотре: app={}, uri={}, ip={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(), endpointHitDto.getIp());

        try {
            if (endpointHitDto.getTimestamp() == null) {
                endpointHitDto.setTimestamp(LocalDateTime.now().format(FORMATTER));
            }

            String url = serverUrl + "/hit";
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    endpointHitDto,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Данные успешно отправлены. Статус: {}", response.getStatusCode());
            } else {
                log.warn("Сервер статистики вернул ошибку при сохранении: статус {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Критическая ошибка при обращении к серверу статистики (POST /hit): {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Запрос статистики с сервера: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", encodeDateTime(start))
                    .queryParam("end", encodeDateTime(end));

            if (uris != null && !uris.isEmpty()) {
                for (String uri : uris) {
                    builder.queryParam("uris", uri);
                }
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.encode().toUriString();
            log.debug("Сформированный URL для запроса статистики: {}", url);

            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
                    url,
                    ViewStatsDto[].class
            );

            if (response.getBody() != null) {
                log.info("Статистика получена успешно. Количество записей: {}", response.getBody().length);
                return Arrays.asList(response.getBody());
            }

            log.info("Сервер вернул пустой результат статистики");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Критическая ошибка при получении статистики (GET /stats): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        return getStats(start.format(FORMATTER), end.format(FORMATTER), uris, unique);
    }

    private String encodeDateTime(String dateTime) {
        try {
            return URLEncoder.encode(dateTime, StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            log.error("Ошибка кодирования даты/времени '{}': {}", dateTime, e.getMessage());
            return dateTime;
        }
    }
}
