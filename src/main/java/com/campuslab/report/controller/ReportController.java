package com.campuslab.report.controller;

import com.campuslab.report.dto.KpisResponseDTO;
import com.campuslab.report.dto.TopResourceDTO;
import com.campuslab.report.repository.ReservaSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API de solo lectura de KPIs, calculados sobre REPORT_RESERVA_SNAPSHOTS
 * (el estado mas reciente de cada reserva, mantenido al dia consumiendo
 * bookings.events). Restringida a ADMIN/AUDITOR, igual que ms-campuslab-audit.
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class ReportController {

    private final ReservaSnapshotRepository repository;

    /**
     * GET /api/report/kpis
     * tasaAprobacion: reservas cuyo estado actual ya paso por APROBADA
     * (APROBADA/EN_PREPARACION/EN_USO/DEVUELTA) sobre el total. tasaCancelacion:
     * reservas actualmente CANCELADA sobre el total. Son una aproximacion
     * basada en el estado ACTUAL (no en si alguna vez pasaron por ese estado),
     * consistente con que report solo mantiene el ultimo snapshot por reserva.
     */
    @GetMapping("/kpis")
    public ResponseEntity<KpisResponseDTO> kpis() {
        long total = repository.count();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        repository.contarPorEstado().forEach(fila -> porEstado.put(fila.getEstado(), fila.getCantidad()));

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
     * GET /api/report/top-resources?limit=10
     * Recursos con mas reservas (de cualquier estado), de mayor a menor.
     */
    @GetMapping("/top-resources")
    public ResponseEntity<List<TopResourceDTO>> topResources(
            @RequestParam(defaultValue = "10") int limit) {

        List<TopResourceDTO> resultado = repository.topRecursos(PageRequest.of(0, limit)).stream()
                .map(fila -> new TopResourceDTO(fila.getRecursoId(), fila.getCantidad()))
                .toList();

        return ResponseEntity.ok(resultado);
    }
}
