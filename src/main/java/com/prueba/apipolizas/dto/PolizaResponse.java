package com.prueba.apipolizas.dto;

import com.prueba.apipolizas.model.Poliza;
import com.prueba.apipolizas.model.enums.EstadoPoliza;
import com.prueba.apipolizas.model.enums.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Representacion de salida de una Poliza.
 */
public record PolizaResponse(
        Long id,
        TipoPoliza tipo,
        EstadoPoliza estado,
        String tomador,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFin,
        Integer mesesVigencia,
        BigDecimal canonMensual,
        BigDecimal prima,
        List<RiesgoResponse> riesgos
) {
    public static PolizaResponse desde(Poliza poliza) {
        List<RiesgoResponse> riesgos = poliza.getRiesgos().stream()
                .map(RiesgoResponse::desde)
                .toList();
        return new PolizaResponse(
                poliza.getId(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getTomador(),
                poliza.getVigenciaInicio(),
                poliza.getVigenciaFin(),
                poliza.getMesesVigencia(),
                poliza.getCanonMensual(),
                poliza.getPrima(),
                riesgos
        );
    }
}
