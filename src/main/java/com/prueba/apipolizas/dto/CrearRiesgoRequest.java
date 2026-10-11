package com.prueba.apipolizas.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos necesarios para agregar un riesgo a una poliza colectiva.
 */
public record CrearRiesgoRequest(

        @NotBlank(message = "La direccion del inmueble es obligatoria")
        String direccionInmueble,

        @NotBlank(message = "El arrendatario es obligatorio")
        String arrendatario
) {
}
