package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.model.EndpointHit;

@Component
public class HitMapper {
    public EndpointHit mapToEndpointHit(RequestHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}