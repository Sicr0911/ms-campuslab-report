package com.campuslab.report.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.SECONDS;

class RangeParserTest {

    @Test
    void sinRange_devuelveNullParaUsarElHistoricoCompleto() {
        assertThat(RangeParser.desde(null)).isNull();
        assertThat(RangeParser.desde("  ")).isNull();
    }

    @Test
    void last24h_restaVeinticuatroHoras() {
        Instant esperado = Instant.now().minus(Duration.ofHours(24));
        assertThat(RangeParser.desde("last24h")).isCloseTo(esperado, within(5, SECONDS));
    }

    @Test
    void last7d_restaSieteDias() {
        Instant esperado = Instant.now().minus(Duration.ofDays(7));
        assertThat(RangeParser.desde("last7d")).isCloseTo(esperado, within(5, SECONDS));
    }

    @Test
    void formatoInvalido_lanzaIllegalArgument() {
        assertThatThrownBy(() -> RangeParser.desde("ayer"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("range invalido");
    }
}
