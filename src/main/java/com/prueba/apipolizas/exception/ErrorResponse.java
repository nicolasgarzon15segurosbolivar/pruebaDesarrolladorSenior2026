package com.prueba.apipolizas.exception;

import java.time.LocalDateTime;

/**
 * Estructura estandar de respuesta de error de la API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
    public static ErrorResponse de(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message);
    }
}
