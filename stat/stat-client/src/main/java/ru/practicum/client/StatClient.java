package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.StatDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class StatClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;

    public StatClient() {
        this.restTemplate = new RestTemplate();
    }

    public void createHit(RequestHitDto requestHitDto) {
        String url = serverUrl + "/hit";

        log.info("Отправка хита на {}: {}", url, requestHitDto);

        HttpEntity<RequestHitDto> request = new HttpEntity<>(requestHitDto, defaultHeaders());

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Хит успешно создан");
            } else {
                log.error("Ошибка создания хита: статус {}", response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            log.error("HTTP ошибка при создании хита: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ошибка при создании хита: {}", e.getMessage(), e);
        }
    }

    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end,
                                  List<String> uris, boolean unique) {

        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }

        String url = builder.build().encode().toUriString();
        log.info("Запрос статистики: {}", url);

        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders());

        try {
            ResponseEntity<List<StatDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            log.info("Получена статистика, количество записей: {}",
                    response.getBody() != null ? response.getBody().size() : 0);

            return response.getBody() != null ? response.getBody() : List.of();

        } catch (HttpClientErrorException e) {
            log.error("HTTP ошибка при получении статистики: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage(), e);
        }

        return List.of();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }
}