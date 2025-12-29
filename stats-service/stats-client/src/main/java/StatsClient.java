import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.Collections;
import java.util.List;

@Component
public class StatsClient {

    private RestTemplate restTemplate;
    private String baseUrl;

    public void statsClientImpl(
            RestTemplate restTemplate,
            @Value("${stats.service.url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public StatsClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * POST /hit
     */

    public void hit(EndpointHitDto endpointHit) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/hit")
                .toUriString();

        HttpEntity<EndpointHitDto> request = new HttpEntity<>(endpointHit);
        restTemplate.postForEntity(url, request, Void.class);
    }

    /**
     * GET /stats
     */

    public List<ViewStatsDto> getStats(
            String start,
            String end,
            List<String> uris,
            Boolean unique
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                builder.build(true).toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody() != null
                ? response.getBody()
                : Collections.emptyList();
    }
}
