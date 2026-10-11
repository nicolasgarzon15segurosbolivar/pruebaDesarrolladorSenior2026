package com.prueba.apipolizas.dto;

import com.prueba.apipolizas.model.Riesgo;
import com.prueba.apipolizas.model.enums.EstadoRiesgo;

/**
 * Representacion de salida de un Riesgo.
 */
public record RiesgoResponse(
        Long id,
        String direccionInmueble,
        String arrendatario,
        EstadoRiesgo estado,
        Long polizaId
) {
    public static RiesgoResponse desde(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getDireccionInmueble(),
                riesgo.getArrendatario(),
                riesgo.getEstado(),
                riesgo.getPoliza() != null ? riesgo.getPoliza().getId() : null
        );
    }
}
