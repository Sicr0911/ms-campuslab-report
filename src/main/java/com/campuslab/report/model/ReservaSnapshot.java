package com.campuslab.report.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Ultimo estado conocido de cada reserva, mantenido al dia consumiendo
 * bookings.events. A diferencia de ms-campuslab-audit (que guarda TODO el
 * historial de eventos), report solo necesita el estado mas reciente por
 * reserva para calcular KPIs (conteos por estado, recursos mas reservados),
 * asi que mantiene una fila por RESERVA_ID en vez de una tabla de eventos
 * en crecimiento indefinido.
 *
 * Es un upsert idempotente ante reentregas de Kafka (at-least-once): un
 * evento solo actualiza la fila si su timestamp es mas nuevo que
 * ULTIMO_EVENTO_EN (ver ReservaSnapshotService), asi que aplicar el mismo
 * evento dos veces no cambia el resultado.
 */
@Entity
@Table(name = "REPORT_RESERVA_SNAPSHOTS")
@Getter
@Setter
@NoArgsConstructor
public class ReservaSnapshot {

    @Id
    @Column(name = "RESERVA_ID")
    private Long reservaId;

    @Column(name = "RECURSO_ID", nullable = false)
    private Long recursoId;

    @Column(name = "USUARIO_ID")
    private Long usuarioId;

    @Column(name = "ESTADO_ACTUAL", nullable = false, length = 20)
    private String estadoActual;

    @Column(name = "ULTIMO_EVENT_TYPE", nullable = false, length = 40)
    private String ultimoEventType;

    @Column(name = "FECHA_CREACION", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "ULTIMO_EVENTO_EN", nullable = false)
    private Instant ultimoEventoEn;
}
