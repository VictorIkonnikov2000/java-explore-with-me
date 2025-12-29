import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class StatsClient {

    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String statsServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(statsServiceUrl)
                .build();
    }

    public void hit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto endpointHit = new EndpointHitDto(null, app, uri, ip, timestamp);

        webClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(endpointHit), EndpointHitDto.class)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> log.info("Hit saved. Status: {}", response.getStatusCode()),
                        error -> log.error("Error saving hit: {}", error.getMessage())
                );
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startEncoded = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
        String endEncoded = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

        StringBuilder uriBuilder = new StringBuilder("/stats?start=" + startEncoded + "&end=" + endEncoded + "&unique=" + unique);
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                uriBuilder.append("&uris=").append(uri);
            }
        }

        return webClient.get()
                .uri(uriBuilder.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .block();
    }
}
