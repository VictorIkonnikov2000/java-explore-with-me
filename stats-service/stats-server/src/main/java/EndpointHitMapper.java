import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

@UtilityClass
public class EndpointHitMapper {
    public static EndpointHitEntity toEntity(EndpointHitDto hit) {
        if (hit == null) {
            return null;
        }
        return EndpointHitEntity.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }
}