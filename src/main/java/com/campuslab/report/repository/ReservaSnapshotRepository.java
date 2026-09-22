package com.campuslab.report.repository;

import com.campuslab.report.model.ReservaSnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservaSnapshotRepository extends JpaRepository<ReservaSnapshot, Long> {

    long countByEstadoActual(String estadoActual);

    @Query("SELECT s.estadoActual AS estado, COUNT(s) AS cantidad " +
            "FROM ReservaSnapshot s GROUP BY s.estadoActual")
    List<ConteoPorEstado> contarPorEstado();

    @Query("SELECT s.recursoId AS recursoId, COUNT(s) AS cantidad " +
            "FROM ReservaSnapshot s GROUP BY s.recursoId ORDER BY COUNT(s) DESC")
    List<ConteoPorRecurso> topRecursos(Pageable pageable);

    interface ConteoPorEstado {
        String getEstado();
        Long getCantidad();
    }

    interface ConteoPorRecurso {
        Long getRecursoId();
        Long getCantidad();
    }
}
