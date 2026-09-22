package com.campuslab.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class KpisResponseDTO {

    private long totalReservas;
    private Map<String, Long> porEstado;
    private double tasaAprobacion;
    private double tasaCancelacion;
}
