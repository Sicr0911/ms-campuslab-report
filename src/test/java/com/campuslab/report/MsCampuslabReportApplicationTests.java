package com.campuslab.report;

import com.campuslab.report.messaging.BookingsEventListener;
import com.campuslab.report.repository.ReservaSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Carga el contexto completo con el perfil "test" (H2 en memoria, sin
 * Kafka): verifica que el mapeo JPA y los repositorios de KPIs arrancan.
 */
@SpringBootTest
@ActiveProfiles("test")
class MsCampuslabReportApplicationTests {

    @Autowired
    private BookingsEventListener listener;

    @Autowired
    private ReservaSnapshotRepository repository;

    @Test
    void contextLoads() {
        assertThat(listener).isNotNull();
        assertThat(repository.contarPorEstado()).isNotNull();
    }
}
