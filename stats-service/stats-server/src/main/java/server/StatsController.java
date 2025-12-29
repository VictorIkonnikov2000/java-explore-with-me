package server;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @RequestMapping(value = "/hit", method = RequestMethod.POST)
    public ResponseEntity<Void> hit(@RequestBody EndpointHitDto endpointHitDto) {
        statsService.saveHit(endpointHitDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false, name = "uris") List<String> uris,
            @RequestParam(defaultValue = "false", required = false, name = "unique") Boolean unique) {

        List<ViewStatsDto> stats = statsService.getStats(start, end, uris, unique);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}
