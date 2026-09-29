package com.campuslab.report.controller;

import com.campuslab.report.dto.KpisResponseDTO;
import com.campuslab.report.dto.TopResourceDTO;
import com.campuslab.report.repository.ReservaSnapshotRepository;
import com.campuslab.report.util.RangeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API de solo lectura de KPIs, calculados sobre REPORT_RESERVA_SNAPSHOTS
 * (el estado mas reciente de cada reserva, mantenido al dia consumiendo
 * bookings.events). Restringida a ADMIN, segun el caso (a diferencia de
 * auditoria, que ademas incluye al Auditor).
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReservaSnapshotRepository repository;

    /**
     * GET /api/report/kpis?range=last24h
     * range es opcional (formato last{N}h o last{N}d); sin el, se calcula
     * sobre el historico completo. Filtra por fechaCreacion de la reserva.
     *
     * tasaAprobacion: reservas cuyo estado actual ya paso por APROBADA
     * (APROBADA/EN_PREPARACION/EN_USO/DEVUELTA) sobre el total. tasaCancelacion:
     * reservas actualmente CANCELADA sobre el total. Son una aproximacion
     * basada en el estado ACTUAL (no en si alguna vez pasaron por ese estado),
     * consistente con que report solo mantiene el ultimo snapshot por reserva.
     */
    @GetMapping("/kpis")
    public ResponseEntity<KpisResponseDTO> kpis(@RequestParam(required = false) String range) {
        Instant desde = RangeParser.desde(range);

        long total = desde == null ? repository.count() : repository.countByFechaCreacionGreaterThanEqual(desde);

        Map<String, Long> porEstado = new LinkedHashMap<>();
        var filas = desde == null ? repository.contarPorEstado() : repository.contarPorEstadoDesde(desde);
        filas.forEach(fila -> porEstado.put(fila.getEstado(), fila.getCantidad()));

        long aprobadasOMasAlla = porEstado.getOrDefault("APROBADA", 0L)
                + porEstado.getOrDefault("EN_PREPARACION", 0L)
                + porEstado.getOrDefault("EN_USO", 0L)
                + porEstado.getOrDefault("DEVUELTA", 0L);
        long canceladas = porEstado.getOrDefault("CANCELADA", 0L);

        double tasaAprobacion = total == 0 ? 0.0 : (double) aprobadasOMasAlla / total;
        double tasaCancelacion = total == 0 ? 0.0 : (double) canceladas / total;

        return ResponseEntity.ok(new KpisResponseDTO(total, porEstado, tasaAprobacion, tasaCancelacion));
    }

    /**
     * GET /api/report/top-resources?range=last7d&limit=10
     * Recursos con mas reservas (de cualquier estado), de mayor a menor.
     * range es opcional, mismo formato que en /kpis.
     */
    @GetMapping("/top-resources")
    public ResponseEntity<List<TopResourceDTO>> topResources(
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "10") int limit) {

        Instant desde = RangeParser.desde(range);
        var filas = desde == null
                ? repository.topRecursos(PageRequest.of(0, limit))
                : repository.topRecursosDesde(desde, PageRequest.of(0, limit));

        List<TopResourceDTO> resultado = filas.stream()
                .map(fila -> new TopResourceDTO(fila.getRecursoId(), fila.getCantidad()))
                .toList();

        return ResponseEntity.ok(resultado);
    }
}
