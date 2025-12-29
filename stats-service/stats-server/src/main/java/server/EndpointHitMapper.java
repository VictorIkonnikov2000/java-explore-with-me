package server;

import dto.EndpointHitDto;
import server.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EndpointHitMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toEntity(EndpointHitDto dto) {
        validateEndpointHit(dto);
        return EndpointHit.builder()
                .app(dto.getApp().trim())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(parseDateTime(dto.getTimestamp()))
                .build();
    }

    private static void validateEndpointHit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto.getApp() == null || endpointHitDto.getApp().trim().isEmpty()) {
            throw new IllegalArgumentException("Название приложения не может быть пустым");
        }
        if (endpointHitDto.getUri() == null || endpointHitDto.getUri().isEmpty()) {
            throw new IllegalArgumentException("URI не может быть пустым");
        }
        if (endpointHitDto.getIp() == null || endpointHitDto.getIp().isEmpty()) {
            throw new IllegalArgumentException("IP не может быть пустым");
        }
    }

    private static LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некорректный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss. Получено: " + dateTime, e);
        }
    }
}
