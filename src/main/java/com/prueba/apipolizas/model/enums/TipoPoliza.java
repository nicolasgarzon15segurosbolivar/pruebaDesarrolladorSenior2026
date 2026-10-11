package com.prueba.apipolizas.model.enums;

/**
 * Tipo de poliza soportado por el negocio.
 * - INDIVIDUAL: el tomador y asegurado es el arrendatario, el beneficiario es el arrendador. Solo 1 riesgo.
 * - COLECTIVA: orientada a inmobiliarias/administraciones. Puede tener uno o muchos riesgos.
 */
public enum TipoPoliza {
    INDIVIDUAL,
    COLECTIVA
}
