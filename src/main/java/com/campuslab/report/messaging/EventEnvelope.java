package com.campuslab.report.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Sobre comun de evento publicado por bookings a Kafka (topico bookings.events),
 * con el mismo formato que el resto de los productores del proyecto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        JsonNode payload
) {
}
