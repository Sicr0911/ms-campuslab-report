package com.campuslab.report.messaging;

import com.campuslab.report.model.ReservaSnapshot;
import com.campuslab.report.repository.ReservaSnapshotRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consume bookings.events y mantiene al dia REPORT_RESERVA_SNAPSHOTS (una
 * fila por reserva con su estado mas reciente), que es lo que consultan los
 * endpoints de KPIs. Idempotente ante reentregas: solo aplica el evento si
 * es mas nuevo que el ultimo aplicado a esa reserva.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingsEventListener {

    private final ReservaSnapshotRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bookings.events", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onMessage(String message) {
        EventEnvelope envelope;
        try {
            envelope = objectMapper.readValue(message, EventEnvelope.class);
        } catch (Exception ex) {
            log.error("Evento de bookings.events con formato invalido, se descarta a la DLT: {}", message, ex);
            throw new IllegalArgumentException("Payload invalido en bookings.events", ex);
        }

        JsonNode payload = envelope.payload();
        Long reservaId = leerLong(payload, "reservaId");
        if (reservaId == null) {
            log.error("Evento {} sin reservaId en el payload, se descarta a la DLT", envelope.eventId());
            throw new IllegalArgumentException("Evento sin reservaId: " + envelope.eventId());
        }

        ReservaSnapshot snapshot = repository.findById(reservaId).orElseGet(() -> {
            ReservaSnapshot nuevo = new ReservaSnapshot();
            nuevo.setReservaId(reservaId);
            nuevo.setFechaCreacion(envelope.timestamp());
            return nuevo;
        });

        // Idempotencia/orden: si ya se aplico un evento mas nuevo (o igual) a
        // esta reserva, este mensaje es una reentrega o llego fuera de orden
        // y no debe pisar el estado ya registrado.
        if (snapshot.getUltimoEventoEn() != null && !envelope.timestamp().isAfter(snapshot.getUltimoEventoEn())) {
            log.info("Evento {} de la reserva {} no es mas nuevo que el ultimo aplicado, se descarta.",
                    envelope.eventId(), reservaId);
            return;
        }

        snapshot.setRecursoId(leerLong(payload, "recursoId"));
        snapshot.setUsuarioId(leerLong(payload, "usuarioSolicitanteId"));
        snapshot.setEstadoActual(leerTexto(payload, "estado"));
        snapshot.setUltimoEventType(envelope.type());
        snapshot.setUltimoEventoEn(envelope.timestamp());

        repository.save(snapshot);
        log.info("Snapshot de la reserva {} actualizado a {} (evento {})",
                reservaId, snapshot.getEstadoActual(), envelope.eventId());
    }

    private Long leerLong(JsonNode payload, String campo) {
        if (payload == null || !payload.hasNonNull(campo)) {
            return null;
        }
        return payload.get(campo).asLong();
    }

    private String leerTexto(JsonNode payload, String campo) {
        if (payload == null || !payload.hasNonNull(campo)) {
            return null;
        }
        return payload.get(campo).asText();
    }
}
