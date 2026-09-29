package com.campuslab.report.util;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta el parametro "range" que pide el caso para los endpoints de
 * reportes (ej. GET /api/report/kpis?range=last24h,
 * GET /api/report/top-resources?range=last7d): formato "last{N}{h|d}".
 * Sin "range", los endpoints devuelven el acumulado historico completo.
 */
public final class RangeParser {

    private static final Pattern PATRON = Pattern.compile("^last(\\d+)([hd])$");

    private RangeParser() {
    }

    /**
     * @return el instante desde el cual filtrar, o null si range es nulo/vacio
     *         (en ese caso el llamador debe usar el historico completo, sin filtro).
     * @throws IllegalArgumentException si range no tiene el formato esperado.
     */
    public static Instant desde(String range) {
        if (range == null || range.isBlank()) {
            return null;
        }
        Matcher m = PATRON.matcher(range.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "range invalido: '" + range + "'. Formato esperado: last{N}h o last{N}d (ej. last24h, last7d)");
        }
        long cantidad = Long.parseLong(m.group(1));
        Duration duracion = "h".equals(m.group(2)) ? Duration.ofHours(cantidad) : Duration.ofDays(cantidad);
        return Instant.now().minus(duracion);
    }
}
