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
        try {
            if (endpointHitDto.getTimestamp() == null) {
                endpointHitDto.setTimestamp(LocalDateTime.now().format(FORMATTER));
            }

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    serverUrl + "/hit",
                    endpointHitDto,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Запрос сохранён: {} {}, статус: {}",
                        endpointHitDto.getApp(), endpointHitDto.getUri(), response.getStatusCode());
            } else {
                log.warn("Неудачное сохранение запроса: статус {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при сохранении запроса: {}", e.getMessage(), e);
        }
    }


    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
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
            log.debug("Запрос статистики по url: {}", url);

            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
                    url,
                    ViewStatsDto[].class
            );

            if (response.getBody() != null) {
                log.debug("Получено {} записей статистики", response.getBody().length);
                return Arrays.asList(response.getBody());
            }
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);
        return getStats(startStr, endStr, uris, unique);
    }

    private String encodeDateTime(String dateTime) {
        try {
            return URLEncoder.encode(dateTime, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // Заменяем + на %20 для читаемости
        } catch (Exception e) {
            log.error("Ошибка при кодировании даты: {}", dateTime, e);
            return dateTime;
        }
    }
}
