package server;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public ResponseEntity<Void> hit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Получен запрос на сохранение информации о запросе: {}", endpointHitDto);
        statsService.saveHit(endpointHitDto);
        log.info("Информация о запросе успешно сохранена");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false, name = "uris") List<String> uris,
            @RequestParam(defaultValue = "false", required = false, name = "unique") Boolean unique) {

        log.info("Получен запрос на получение статистики с параметрами: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        List<ViewStatsDto> stats = statsService.getStats(start, end, uris, unique);
        log.info("Статистика успешно получена. Количество записей: {}", stats.size());
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}

